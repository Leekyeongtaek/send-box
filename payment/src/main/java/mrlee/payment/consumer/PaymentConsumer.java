package mrlee.payment.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.global.dto.OrderCreatedEvent;
import mrlee.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "msa-orders-event", groupId = "payment-group")
    public void consume(String payload) {
        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);
        log.info("Received OrderCreatedEvent {}", orderCreatedEvent);
        try {
            paymentService.requestPayment(orderCreatedEvent.totalAmount().doubleValue());
        } catch (Exception e) {

        }
    }
}
