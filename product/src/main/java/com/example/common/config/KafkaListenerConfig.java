package com.example.common.config;

import com.example.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaListenerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public DefaultErrorHandler customErrorHandler() {
        // 1초 간격, 최대 3번 재시도
        FixedBackOff backOff = new FixedBackOff(1_000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> {
                    log.error("[Kafka Error] topic={}, key={}, value={}, message={}",
                            record.topic(), record.key(), record.value(), ex.getMessage(), ex);
                },
                backOff
        );

        // CustomException은 재시도하지 않게 설정
        errorHandler.addNotRetryableExceptions(CustomException.class);

        return errorHandler;
    }
}
