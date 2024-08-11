package com.apachekafka.controller;

import com.apachekafka.domain.LibraryEvent;
import com.apachekafka.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    @Autowired
    public LibraryEventController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(
            @RequestBody LibraryEvent libraryEvent
    ) throws JsonProcessingException {
        log.info("libraryEvent : {}", libraryEvent);

        libraryEventProducer.sendLibraryEvent(libraryEvent);
//        libraryEventProducer.sendLibraryEventProducerRecord(libraryEvent);

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
