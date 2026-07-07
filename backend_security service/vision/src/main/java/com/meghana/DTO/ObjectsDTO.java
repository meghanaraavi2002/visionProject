package com.meghana.DTO;

import com.meghana.enums.Status;

public class ObjectsDTO {

    private Long id;
    private Long track;
    private String name;
    private Status status;
    private String timeStamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTrack() {
        return track;
    }

    public void setTrack(Long track) {
        this.track = track;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "ObjectsDTO [id=" + id +
                ", tId=" + track +
                ", name=" + name +
                ", status=" + status +
                ", timeStamp=" + timeStamp + "]";
    }
}