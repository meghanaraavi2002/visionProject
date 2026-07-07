package com.stark.entity;

import com.meghana.enums.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "OBJECTS")
public class Objects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TRACK")
    private Long track;

    @Column(name = "OBJECT_NAME")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status status;

    @Column(name = "TIME")
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
		return "Objects [id=" + id + ", tId=" + track + ", name=" + name + ", status=" + status + ", timeStamp="
				+ timeStamp + "]";
	}

    
}