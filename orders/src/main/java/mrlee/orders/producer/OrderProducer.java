package mrlee.orders.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.global.dto.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

//    public void sendOrderCreatedEvent(String aggregateId, String payload) {
//        // 메시지 키(Key) 설정: kafkaTemplate.send()의 두 번째 인자로 orderId를 메시지 키로 전달하면,
//        // 동일 주문에 대한 이벤트 순서가 동일 파티션 내에서 보장됩니다.
//        kafkaTemplate.send("msa-orders-event", aggregateId, payload);
//    }

    public CompletableFuture<SendResult<String, Object>> sendOrderCreatedEvent(String aggregateId, String payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("msa-orders-event", aggregateId, payload);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("카프카 메시지 전송 성공. Topic: {}, Offset: {}", result.getRecordMetadata().topic(), result.getRecordMetadata().offset());
            } else {
                log.error("카프카 메시지 전송 실패. aggregateId: {}", aggregateId, ex);
            }
        });

        return future;
    }

}
