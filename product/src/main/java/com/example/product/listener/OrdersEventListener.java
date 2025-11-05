package com.example.product.listener;

import com.example.common.events.orders.OrdersCompleteEvent;
import com.example.common.events.orders.OrdersCreateEvent;
import com.example.common.events.orders.OrdersFailEvent;
import com.example.common.exception.CustomException;
import com.example.common.message.orders.OrdersCompleteMessage;
import com.example.common.message.orders.OrdersCreateMessage;
import com.example.common.message.orders.OrdersFailMessage;
import com.example.product.domain.Product;
import com.example.product.reposiory.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OrdersEventListener {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    //여러 예외 상황에 대한 처리는 생략
    //유틸 클래스 사용 안함, 데이터 유효성 검증 안함
    @KafkaListener(
            topics = "ordering.orders-created",
            groupId = "ordering.orders-created.consumer",
            concurrency = "2")
    public void handleOrdersCreatedEvent(String message) throws JsonProcessingException {
        log.info("Received Orders Created message: {}", message);
        OrdersCreateMessage ordersCreateMessage = objectMapper.readValue(message, OrdersCreateMessage.class);
        OrdersCreateEvent ordersCreateEvent = ordersCreateMessage.getPayload();
        try {
            List<Long> productIds = ordersCreateEvent.getProductIds();
            Map<Long, Product> productMap = productRepository.findAllById(productIds)
                    .stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));

            for (OrdersCreateEvent.OrderItem orderItem : ordersCreateEvent.getOrderItems()) {
                Product product = productMap.get(Long.valueOf(orderItem.getProductId()));
                product.decreaseStockQuantity(orderItem.getQuantity());
            }

            OrdersCompleteEvent ordersCompleteEvent = new OrdersCompleteEvent(ordersCreateEvent.getOrderId());
            kafkaTemplate.send("ordering.orders-completed", new OrdersCompleteMessage(ordersCompleteEvent));
        } catch (CustomException ce) { // 트랜잭션(@Transactional)은 RuntimeException 타입 발생시 롤백되나 던지지 않으면 롤백 되지 않음.
            OrdersFailEvent ordersFailEvent = new OrdersFailEvent(ordersCreateEvent.getOrderId(), ce.getMessage());
            kafkaTemplate.send("ordering.orders-failed", new OrdersFailMessage(ordersFailEvent));
            throw ce;
        } catch (Exception e) {
            log.error("Error occurred while processing Orders Created message : {}", e.getMessage());
            throw e;
        }
    }
}
