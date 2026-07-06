import cv2
from ultralytics import YOLO
import requests
from datetime import datetime
# #Read Image
# image=cv2.imread('image.jpeg')
# #show image
# cv2.imshow('My Image',image)
# cv2.waitKey(0)
# cv2.destroyAllWindows()

# Conceptual layout of what we will send:



cam=cv2.VideoCapture(0)
model=YOLO('yolov8n.pt')
li=['person','cell phone','cat','dog','laptop']
currently_tracked=set()
while True:
    ret,frame=cam.read()
    if not ret:
        break
    result=model(frame)
    frame_prediction=result[0]
    seen_this_frame=[]
    for box in frame_prediction.boxes:
        x1,y1,x2,y2=map(int,box.xyxy[0])
        cls_id=int(box.cls[0])
        label_name=model.names[cls_id]
        if label_name not in li:
            continue
        seen_this_frame.append(label_name)
        cv2.rectangle(frame,(x1,y1),(x2,y2),(0,255,0),2)
        cv2.putText(frame,label_name,(x1,y1-10),cv2.FONT_HERSHEY_SIMPLEX,0.5,(255,0,0),2)    
    # Conceptual endpoint layout:
    url = "http://127.0.0.1:8080/api/v1/security/event"
    # response = requests.post(url, json=payload)    
    for object in seen_this_frame:
        if object not in currently_tracked:
            live_now = datetime.utcnow().isoformat() + "Z"
            payload = {
                "name": object,
                "status": "ENTERED", # or "LEFT"
                 "timeStamp": live_now # We can add real timestamps later
            }
            response = requests.post(url, json=payload)
            currently_tracked.add(object)
    to_remove=set()
    for object in currently_tracked:
        if object not in seen_this_frame:
            live_now = datetime.utcnow().isoformat() + "Z"
            payload = {
                "name": object,
                "status": "LEFT", 
                 "timeStamp": live_now # We can add real timestamps later
            }
            response = requests.post(url, json=payload)
            to_remove.add(object)
    currently_tracked=currently_tracked-to_remove
    print(currently_tracked)
    cv2.imshow('Stark Lab Vision',frame)
    key=cv2.waitKey(30)
    if key==ord('q'):
        break
cam.release()
cv2.destroyAllWindows()
cv2.waitKey(1)