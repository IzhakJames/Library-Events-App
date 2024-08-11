package com.apachekafka.producer;

import com.apachekafka.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Value("${spring.kafka.topic}")
    private String topic;
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public LibraryEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        CompletableFuture<SendResult<Integer,String>> result = kafkaTemplate.send(topic, key,value);

        result.whenCompleteAsync(((sendResult, throwable) -> {
            if (throwable != null) {
                log.error("Error occurred during sending of Message. Error : {}", throwable.getMessage(), throwable);
            } else {
                log.info("Successfully sent message with key : {} and value : {}", key, value);
            }
        }));
    }

    public void sendLibraryEventSync(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        SendResult<Integer,String> result = kafkaTemplate.send(topic, key,value).get();
        log.info("Successfully sent message with key : {} and value : {}", key, value);


    }

    public void sendLibraryEventProducerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> record = buildProducerRecord(key, value);

        CompletableFuture<SendResult<Integer,String>> result = kafkaTemplate.send(record);

        result.whenCompleteAsync(((sendResult, throwable) -> {
            if (throwable != null) {
                log.error("Error occurred during sending of Message. Error : {}", throwable.getMessage(), throwable);
            } else {
                log.info("Successfully sent message with key : {} and value : {}", key, value);
            }
        }));
    }

    public ProducerRecord<Integer,String> buildProducerRecord(Integer key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }

}
