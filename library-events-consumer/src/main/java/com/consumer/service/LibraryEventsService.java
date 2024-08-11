package com.consumer.service;

import com.consumer.entity.LibraryEvent;
import com.consumer.entity.LibraryEventType;
import com.consumer.repository.LibraryEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryEventsService {

    ObjectMapper objectMapper;
    LibraryEventsRepository libraryEventsRepository;

    @Autowired
    public LibraryEventsService(ObjectMapper objectMapper, LibraryEventsRepository libraryEventsRepository) {
        this.objectMapper = objectMapper;
        this.libraryEventsRepository = libraryEventsRepository;
    }

    public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                libraryEvent.getBook().setLibraryEvent(libraryEvent);
                libraryEventsRepository.save(libraryEvent);
                log.info("Successfully saved the library event: {}", libraryEvent);
                break;
            case UPDATE:
                if (libraryEvent.getLibraryEventId() == null) {
                    log.error("Library Event ID cannot be null");
                    throw new IllegalArgumentException("Library Event ID cannot be null");
                }
                LibraryEvent event = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).orElse(null);
                if (event == null) {
                    log.error("Library Event does not exits in database");
                    throw new IllegalArgumentException("Not a valid ID");
                }
                else {
                    libraryEvent.getBook().setLibraryEvent(libraryEvent);
                    libraryEventsRepository.save(libraryEvent);
                }
                break;
            default:
                log.info("Invalid Library Event Type");
        }

    }
}
