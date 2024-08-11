package com.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {


    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    public DefaultErrorHandler errorHandler() {

        // Changes the retries from the given attempts to 0
        List<Class<IllegalArgumentException>> exceptionsToIgnoreList = List.of(
                IllegalArgumentException.class);

        // Make the retries for the calls maximum of 3 times, 1 original and 2 re-attempts
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

        ExponentialBackOff exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1_000L);
        exponentialBackOff.setMultiplier(2);
        exponentialBackOff.setMaxInterval(2_000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
//                fixedBackOff
                exponentialBackOff
        );

        exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            log.info("Failed Record in Retry Listener, Exception : {} , deliveryAttempt : {}",
                    ex.getMessage(),
                    deliveryAttempt);
        }));

        return errorHandler;
    }
}
