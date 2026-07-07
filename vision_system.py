import cv2
from ultralytics import YOLO
import requests
from datetime import datetime
import time
from deepface import DeepFace
import os


cam = cv2.VideoCapture(0)
model = YOLO('yolov8n.pt')
li = ['person', 'cell phone', 'cat', 'dog', 'laptop']

# Structure: { "person": {"status": "ENTERED", "last_changed": timestamp} }
tracked_states = {}
DEBOUNCE_DELAY_SECONDS = 2.0  # Time an object must be missing/present to change state

url = "http://127.0.0.1:8080/api/v1/security/event"
DB_PATH="./known_faces"
if not os.path.exists(DB_PATH):
    os.makedirs(DB_PATH)

def indentify_face_profile(frame,box_coords):
    x1,y1,x2,y2=box_coords
    cropped_image=frame[y1:y2,x1:x2]
    if cropped_image.size==0 or len(os.listdir(DB_PATH))==0:
        return "Unknown"
    #result_df=DeepFace.find(img_path=cropped_image,db_path="./known_faces",enforce_detection=False,silent=True)
    result_df = DeepFace.find(img_path=cropped_image, db_path="./known_faces", detector_backend="retinaface", enforce_detection=False, silent=True)
    if len(result_df)>0 and not result_df[0].empty:
        matched_path=result_df[0]['identity'][0]
        print(matched_path)
        return matched_path.split("known_faces\\")[-1].split(".")[0]
    return "Unknown"

while True:
    ret, frame = cam.read()
    if not ret:
        break
        
    result = model.track(frame,persist=True)
    frame_prediction = result[0]
    
    # Use a set for O(1) lookups during this specific frame
    seen_this_frame = {}
    if frame_prediction.boxes.id is not None :
        for box in frame_prediction.boxes:
                track_id=int(box.id[0].item())

                x1, y1, x2, y2 = map(int, box.xyxy[0])
                cls_id = int(box.cls[0])
                label_name = model.names[cls_id]
                
                if label_name not in li:
                    continue   
                seen_this_frame[track_id]={
                    "label":label_name,
                    "coords":(x1,y1,x2,y2)
                }
                cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                cv2.putText(frame, label_name, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2)    

    current_time = time.time()
    live_now = datetime.utcnow().isoformat() + "Z"

    # 1. Check for newly ENTERED or re-entering objects
    for track_id in seen_this_frame:
        if seen_this_frame[track_id]["label"]=="person":
            resolved_name=indentify_face_profile(frame,seen_this_frame[track_id]["coords"])
            resolved_name=resolved_name if resolved_name!="Unknown" else f"Visitor_{track_id}"
            print("-----------------------------------------------------------------------------------------------------------------------------------------------",resolved_name)
        else:
            resolved_name=seen_this_frame[track_id]["label"]
        if track_id not in tracked_states:
            # Brand new object discovered
            tracked_states[track_id] = {
                "name":resolved_name,
                "status": "ENTERED", 
                "last_changed": current_time}
            payload = {
                "track":track_id,
                "name": resolved_name,
                  "status": "ENTERED", 
                  "timeStamp": live_now
                  }
            try:
                print("---------------------------------------------",payload)
                requests.post(url, json=payload)
            except Exception as e:
                print(f"Error sending payload: {e}")
        else:
            # Object was previously marked as LEFT, check if it's stable before letting it re-enter
            if tracked_states[track_id]["status"] == "LEFT":
                if (current_time - tracked_states[track_id]["last_changed"]) >= DEBOUNCE_DELAY_SECONDS:
                    tracked_states[track_id]["status"] = "ENTERED"
                    tracked_states[track_id]["last_changed"] = current_time
                    payload = {"track":track_id,"name": resolved_name, "status": "ENTERED", "timeStamp": live_now}
                    try:
                        print("---------------------------------------------",payload)
                        requests.post(url, json=payload)
                    except Exception as e:
                        print(f"Error sending payload: {e}")

    # 2. Check for missing objects that might have LEFT
    for track_id in list(tracked_states.keys()):
        if track_id not in seen_this_frame:
            # Object was previously active, check if it's gone long enough to fire LEFT
            if tracked_states[track_id]["status"] == "ENTERED":
                if (current_time - tracked_states[track_id]["last_changed"]) >= DEBOUNCE_DELAY_SECONDS:
                    tracked_states[track_id]["status"] = "LEFT"
                    tracked_states[track_id]["last_changed"] = current_time
                    payload = {"track":track_id,"name": tracked_states[track_id]["name"],"status": "LEFT", "timeStamp": live_now}
                    try:
                        print("---------------------------------------------",payload)
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