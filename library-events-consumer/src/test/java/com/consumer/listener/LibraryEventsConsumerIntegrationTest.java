package com.consumer.listener;

import com.consumer.entity.Book;
import com.consumer.entity.LibraryEvent;
import com.consumer.entity.LibraryEventType;
import com.consumer.repository.LibraryEventsRepository;
import com.consumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = "library-events", partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsConsumerIntegrationTest {

    EmbeddedKafkaBroker embeddedKafkaBroker;

    KafkaTemplate<Integer, String> kafkaTemplate;

    KafkaListenerEndpointRegistry endpointRegistry;

    LibraryEventsRepository libraryEventsRepository;

    ObjectMapper objectMapper;

    @Autowired
    public LibraryEventsConsumerIntegrationTest(EmbeddedKafkaBroker embeddedKafkaBroker, KafkaTemplate<Integer, String> kafkaTemplate, KafkaListenerEndpointRegistry endpointRegistry, LibraryEventsRepository libraryEventsRepository, ObjectMapper objectMapper) {
        this.embeddedKafkaBroker = embeddedKafkaBroker;
        this.kafkaTemplate = kafkaTemplate;
        this.endpointRegistry = endpointRegistry;
        this.libraryEventsRepository = libraryEventsRepository;
        this.objectMapper = objectMapper;
    }

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;

    @SpyBean
    LibraryEventsService libraryEventsServiceSpy;



    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Given
        String json = " {\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        kafkaTemplate.sendDefault(json).get();

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventsRepository.findAll();
        assert libraryEventList.size() == 1;
        libraryEventList.forEach(libraryEvent -> {
            assert libraryEvent.getLibraryEventId() != null;
            assertEquals(456, libraryEvent.getBook().getBookId());
        });

    }

    @Test
    void publishUpdateLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Given
        String json = " {\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);

        Book updatedBook = Book.builder().bookId(456).bookName("Kafka Using Spring Boot Vol 2").bookAuthor("Dilip").build();
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEvent.setBook(updatedBook);

        String updatedJson = objectMapper.writeValueAsString(libraryEvent);

        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();


        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
//        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
//        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
        LibraryEvent event = (LibraryEvent) libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).orElse(null);
        assert event != null;
        assertEquals(456, event.getBook().getBookId());
        assertEquals("Kafka Using Spring Boot Vol 2", event.getBook().getBookName());


    }

    @Test
    void publishNullLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Given
        String json = " {\"libraryEventId\":null,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        kafkaTemplate.sendDefault(json).get();


        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        // Then
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
    }
}
