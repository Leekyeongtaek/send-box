package mrlee.inventory.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.global.dto.OrderCreatedEvent;
import mrlee.inventory.service.InventoryService;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    //todo concurrency 설정 테스트
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
            //,exclude = {IllegalArgumentException.class}
    )
    @KafkaListener(topics = "msa-orders-event", groupId = "inventory-group", concurrency = "3")
    public void consume(String payload) throws InterruptedException {
        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);

        log.info("Received OrderCreatedEvent {}", orderCreatedEvent);

        Thread.sleep(300); // 0.3초 소요된다고 가정
        inventoryService.decreaseStock(orderCreatedEvent.orderId(), orderCreatedEvent.productId());
    }

    // 3번의 시도가 모두 실패하면 최종적으로 이 메서드가 호출됩니다 (DLQ 역할).
    @DltHandler
    public void processDltMessage(String payload,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("재고 차감 최종 실패 (DLT). Payload: {}, Topic: {}, Error: {}", payload, topic, errorMessage);
        // TODO: 관리자가 수동으로 복구할 수 있도록 실패한 데이터를 DB에 기록하거나 알림(Slack 등)을 발송하는 로직을 추가합니다.
    }
}
