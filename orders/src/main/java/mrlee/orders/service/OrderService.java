package mrlee.orders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.global.dto.OrderCreatedEvent;
import mrlee.orders.domain.Order;
import mrlee.orders.domain.OutboxEvent;
import mrlee.orders.repository.OrderRepository;
import mrlee.orders.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void createOrder(OrderCreatedEvent orderCreatedEvent) {
        log.info("Creating order {}", orderCreatedEvent);
        //주문 생성
        Order order = Order.builder()
                .userId(orderCreatedEvent.userId())
                .totalAmount(orderCreatedEvent.totalAmount())
                .status("ORDERED")
                .createdAt(LocalDateTime.now())
                .build();

        Order saveOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saveOrder.getOrderId(),
                saveOrder.getUserId(),
                saveOrder.getTotalAmount(),
                orderCreatedEvent.productId(),
                orderCreatedEvent.quantity());

        try {
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateId(saveOrder.getOrderId().toString())
                    .eventType("OrderCreated")
                    .payload(objectMapper.writeValueAsString(event))
                    .build();

            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Event Outbox save failed", e);
        }
    }
}
