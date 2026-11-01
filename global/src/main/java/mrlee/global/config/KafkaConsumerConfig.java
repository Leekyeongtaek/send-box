package mrlee.global.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9091,localhost:9092,localhost:9093";

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 가장 처음 메시지부터 읽기

        // 1. 실제 데이터를 변환할 JSON 디시리얼라이저 생성 및 신뢰 패키지 설정
        JacksonJsonDeserializer<Object> jsonDeserializer = new JacksonJsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("*");

        // 2. 🚨 JSON 디시리얼라이저를 ErrorHandlingDeserializer로 안전하게 감싸기 (Wrapper)
        ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        // 3. 팩토리에 StringDeserializer와 에러 핸들링이 적용된 디시리얼라이저를 직접 주입
        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                errorHandlingDeserializer // 기존 jsonDeserializer 대신 주입
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
