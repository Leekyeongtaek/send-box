package com.example.common.message;

import com.example.common.events.OrdersFailEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdersFailMessage {

    private String messageId;
    private Long publishedTime;
    private OrdersFailEvent payload;

    public OrdersFailMessage(OrdersFailEvent payload) {
        this.messageId = UUID.randomUUID().toString();
        this.publishedTime = System.currentTimeMillis();
        this.payload = payload;
    }
}
