package com.meghana.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.meghana.DTO.ObjectsDTO;

public interface ObjectService {

	ObjectsDTO saveEvent(ObjectsDTO objectsDto);
	SseEmitter createSseConnection();
}
