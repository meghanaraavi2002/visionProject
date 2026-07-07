package com.meghana.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.meghana.DTO.ObjectsDTO;
import com.meghana.services.ObjectService;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/api/v1/security")
public class ObjectApi {
	
	@Autowired 
	private ObjectService objectSer;
	
	@PostMapping("/event")
	public ResponseEntity<ObjectsDTO > receiveTelemetry(@RequestBody ObjectsDTO objectsDto){
		System.out.println("received payload:"+objectsDto);
		System.out.println("[BACKEND LOG] Received payload from Python:"+objectsDto.getName()+"status:"+objectsDto.getStatus()+"trackID:"+objectsDto.getTrack());
		ObjectsDTO savedDto=objectSer.saveEvent(objectsDto);
		return new ResponseEntity<>(savedDto,HttpStatus.CREATED);
		
	}
	@GetMapping(value="/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamLiveTelemetry() {
		System.out.println("[BACKEND LOG] Angular UI dashboard opened a stream connection.[BACKEND LOG] Angular UI dashboard opened a stream connection.");
		return objectSer.createSseConnection();
	}

}
