import cv2
from ultralytics import YOLO
import requests
from datetime import datetime
import time

cam = cv2.VideoCapture(0)
model = YOLO('yolov8n.pt')
li = ['person', 'cell phone', 'cat', 'dog', 'laptop']

# Structure: { "person": {"status": "ENTERED", "last_changed": timestamp} }
tracked_states = {}
DEBOUNCE_DELAY_SECONDS = 2.0  # Time an object must be missing/present to change state

url = "http://127.0.0.1:8080/api/v1/security/event"

while True:
    ret, frame = cam.read()
    if not ret:
        break
        
    result = model(frame)
    frame_prediction = result[0]
    
    # Use a set for O(1) lookups during this specific frame
    seen_this_frame = set()
    
    for box in frame_prediction.boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        cls_id = int(box.cls[0])
        label_name = model.names[cls_id]
        
        if label_name not in li:
            continue
            
        seen_this_frame.add(label_name)
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(frame, label_name, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2)    

    current_time = time.time()
    live_now = datetime.utcnow().isoformat() + "Z"

    # 1. Check for newly ENTERED or re-entering objects
    for obj_name in seen_this_frame:
        if obj_name not in tracked_states:
            # Brand new object discovered
            tracked_states[obj_name] = {"status": "ENTERED", "last_changed": current_time}
            payload = {"name": obj_name, "status": "ENTERED", "timeStamp": live_now}
            try:
                requests.post(url, json=payload)
            except Exception as e:
                print(f"Error sending payload: {e}")
        else:
            # Object was previously marked as LEFT, check if it's stable before letting it re-enter
            if tracked_states[obj_name]["status"] == "LEFT":
                if (current_time - tracked_states[obj_name]["last_changed"]) >= DEBOUNCE_DELAY_SECONDS:
                    tracked_states[obj_name]["status"] = "ENTERED"
                    tracked_states[obj_name]["last_changed"] = current_time
                    payload = {"name": obj_name, "status": "ENTERED", "timeStamp": live_now}
                    try:
                        requests.post(url, json=payload)
                    except Exception as e:
                        print(f"Error sending payload: {e}")

    # 2. Check for missing objects that might have LEFT
    for obj_name in list(tracked_states.keys()):
        if obj_name not in seen_this_frame:
            # Object was previously active, check if it's gone long enough to fire LEFT
            if tracked_states[obj_name]["status"] == "ENTERED":
                if (current_time - tracked_states[obj_name]["last_changed"]) >= DEBOUNCE_DELAY_SECONDS:
                    tracked_states[obj_name]["status"] = "LEFT"
                    tracked_states[obj_name]["last_changed"] = current_time
                    payload = {"name": obj_name, "status": "LEFT", "timeStamp": live_now}
                    try:
                        requests.post(url, json=payload)
                    except Exception as e:
                        print(f"Error sending payload: {e}")

    # Print current active tracking states to monitor performance in terminal
    active_now = [k for k, v in tracked_states.items() if v["status"] == "ENTERED"]
    print(f"Currently Active: {active_now}")
    
    cv2.imshow('Stark Lab Vision', frame)
    key = cv2.waitKey(30)
    if key == ord('q'):
        break

cam.release()
cv2.destroyAllWindows()
cv2.waitKey(1)