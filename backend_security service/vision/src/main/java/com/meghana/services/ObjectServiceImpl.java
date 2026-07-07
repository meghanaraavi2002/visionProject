package com.meghana.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.meghana.DTO.ObjectsDTO;
import com.meghana.repository.ObjectRepository;
import com.stark.entity.Objects;

import jakarta.transaction.Transactional;

@Service(value = "ObjectSerivce")
@Transactional
public class ObjectServiceImpl implements ObjectService {
	
	@Autowired
	private ObjectRepository repository; // Your injected data repository
	
	@Autowired
	private ModelMapper modelMapper;
	
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	@Override
	public ObjectsDTO saveEvent(ObjectsDTO objectsDto) {
		// 1. Check if this tracked target name already has a history record in our DB
		Optional<Objects> lastRecordedEvent = repository.findByTrackAndName(objectsDto.getTrack(),objectsDto.getName());
		
		if (lastRecordedEvent.isPresent()) {
			Objects historicalEvent = lastRecordedEvent.get();
			
			// 2. State Duplication Gate: If the current status matches the past status, skip processing
			if (historicalEvent.getStatus().equals(objectsDto.getStatus())) {
				System.out.println("[TELEMETRY SERVICE] Dropped duplicate tracking frame for: " 
						+ objectsDto.getName() + " [" + objectsDto.getStatus() + "]");
				return null; // Return null so the controller layer knows it was a duplicate frame
			}
			else {
				historicalEvent.setStatus(objectsDto.getStatus());
				Objects changedEntity=this.repository.save(historicalEvent);
				objectsDto.setId(changedEntity.getId());
				return emit(objectsDto);
			}
		}

		// 3. Complete processing for legitimate state transitions (or entirely new entities)
		Objects entity = this.modelMapper.map(objectsDto, Objects.class);
		Objects savedEntity = this.repository.save(entity);
		
		// Map the newly generated auto-increment database ID back to your DTO object
		objectsDto.setId(savedEntity.getId());
		
		// 4. Broadcast the clean state transition alert live to all open Angular client dashboard screens
//		for (SseEmitter emitter : emitters) {
//			try {
//				emitter.send(objectsDto, MediaType.APPLICATION_JSON);
//			} catch (IOException e) {
//				this.emitters.remove(emitter);
//			}
//		}
//		
//		System.out.println("[TELEMETRY SERVICE] Successfully persisted and streamed new state event for: " + objectsDto.getName());
		return emit(objectsDto);
	}

	@Override
	public SseEmitter createSseConnection() {
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		this.emitters.add(emitter);
		
		emitter.onCompletion(() -> this.emitters.remove(emitter));
		emitter.onTimeout(() -> this.emitters.remove(emitter));
		
		return emitter;
	}
	public ObjectsDTO emit(ObjectsDTO obj) {
		
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(obj, MediaType.APPLICATION_JSON);
			} catch (IOException e) {
				this.emitters.remove(emitter);
			}
		}
		
		System.out.println("[TELEMETRY SERVICE] Successfully persisted and streamed new state event for: " + obj.getName());
		return obj;
	}
}