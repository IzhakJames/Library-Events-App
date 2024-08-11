package com.consumer.listener;

import com.consumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    private LibraryEventsService libraryEventsService;

    @Autowired
    public LibraryEventsConsumer(LibraryEventsService libraryEventsService) {
        this.libraryEventsService = libraryEventsService;
    }

    @KafkaListener(topics = "library-events")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

        log.info("ConsumerRecord : {}", consumerRecord);

        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
