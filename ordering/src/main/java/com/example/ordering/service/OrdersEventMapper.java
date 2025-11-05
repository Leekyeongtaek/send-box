package com.example.ordering.service;

import com.example.common.events.OrdersCreateEvent;
import com.example.common.message.OrdersCreateMessage;
import com.example.ordering.dto.OrdersReqDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrdersEventMapper {

    public OrdersCreateMessage toOrdersCreateEvent(Long orderId, OrdersReqDto ordersReqDto) {
        List<OrdersCreateEvent.OrderItem> orderItems = ordersReqDto.getOrdersItems()
                .stream()
                .map(item -> OrdersCreateEvent.OrderItem.builder()
                        .productId(String.valueOf(item.getProductId()))
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrdersCreateEvent ordersCreateEvent = OrdersCreateEvent.builder()
                .orderId(String.valueOf(orderId))
                .orderItems(orderItems)
                .build();

        return new OrdersCreateMessage(ordersCreateEvent);
    }
}
