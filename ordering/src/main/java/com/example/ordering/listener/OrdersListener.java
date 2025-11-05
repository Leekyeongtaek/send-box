package com.example.ordering.listener;

import com.example.common.events.OrdersCompleteEvent;
import com.example.common.events.OrdersFailEvent;
import com.example.common.message.OrdersCompleteMessage;
import com.example.common.message.OrdersFailMessage;
import com.example.ordering.domain.Orders;
import com.example.ordering.domain.OrdersFailLog;
import com.example.ordering.repository.OrdersFailLogRepository;
import com.example.ordering.repository.OrdersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OrdersListener {

    private final OrdersRepository ordersRepository;
    private final OrdersFailLogRepository ordersFailLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ordering.orders-completed", groupId = "ordering.orders-completed.consumer", concurrency = "2")
    public void handleOrdersCompleteEvent(String message) throws JsonProcessingException {
        log.info("Received OrdersCompleteEvent {}", message);
        OrdersCompleteMessage ordersCompleteMessage = objectMapper.readValue(message, OrdersCompleteMessage.class);
        OrdersCompleteEvent payload = ordersCompleteMessage.getPayload();
        Orders orders = ordersRepository.findById(Long.valueOf(payload.getOrderId()))
                .orElseThrow(EntityNotFoundException::new);
        orders.complete();
    }

    @KafkaListener(topics = "ordering.orders-failed", groupId = "ordering.orders-failed.consumer", concurrency = "2")
    public void handleOrdersFailEvent(String message) throws JsonProcessingException {
        log.info("Received OrdersFailEvent {}", message);
        OrdersFailMessage ordersFailMessage = objectMapper.readValue(message, OrdersFailMessage.class);
        OrdersFailEvent payload = ordersFailMessage.getPayload();
        Orders orders = ordersRepository.findById(Long.valueOf(payload.getOrderId()))
                .orElseThrow(EntityNotFoundException::new);
        orders.fail();
        OrdersFailLog ordersFailLog = new OrdersFailLog(Long.valueOf(payload.getOrderId()), payload.getFailMessage());
        ordersFailLogRepository.save(ordersFailLog);
    }
}
