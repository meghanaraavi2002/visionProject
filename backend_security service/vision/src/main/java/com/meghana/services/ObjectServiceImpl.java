package com.meghana.services;

import java.io.IOException;
import java.util.List;
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
public class ObjectServiceImpl implements ObjectService{
	
	@Autowired
	private ObjectRepository objectRepo;
	private final List<SseEmitter> emitters=new CopyOnWriteArrayList<>();
	@Autowired
	private ModelMapper modelMapper;
	@Override
	public ObjectsDTO saveEvent(ObjectsDTO objectsDto) {
		// TODO Auto-generated method stub
		Objects entity= this.modelMapper.map(objectsDto,Objects.class);
		Objects savedEntity=this.objectRepo.save(entity);
		objectsDto.setId(savedEntity.getId());
		for(SseEmitter emitter:emitters) {
			try {
				emitter.send(objectsDto, MediaType.APPLICATION_JSON);
				
			}catch(IOException e) {
				this.emitters.remove(emitter);
				
			}
		}
		
		
		return objectsDto;
	}

	@Override
	public SseEmitter createSseConnection() {
		// TODO Auto-generated method stub
		SseEmitter emitter=new SseEmitter(Long.MAX_VALUE);
		this.emitters.add(emitter);
		emitter.onCompletion(()->this.emitters.remove(emitter));
		emitter.onTimeout(()->this.emitters.remove(emitter));
		return emitter;
	}

}
