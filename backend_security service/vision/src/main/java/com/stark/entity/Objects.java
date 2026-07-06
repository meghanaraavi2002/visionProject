package com.stark.entity;

import com.meghana.enums.Status;

import jakarta.persistence.Column;
// 1. Fix the imports to target the persistence package
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


@Entity // 2. Tell Jakarta that this is a database entity
@Table(name = "OBJECTS") // Fixed typo from OBJETCS to OBJECTS
public class Objects {

    @Id // 3. Every JPA entity requires a Primary Key field
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="OBJECT_NAME")
    private String name;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(name="TIME")
    private String timeStamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
    
    
    
    
    
}