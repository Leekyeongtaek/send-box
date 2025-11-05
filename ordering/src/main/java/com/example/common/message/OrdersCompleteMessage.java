package com.example.common.message;

import com.example.common.events.OrdersCompleteEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdersCompleteMessage {

    private String messageId;
    private Long publishedTime;
    private OrdersCompleteEvent payload;

    public OrdersCompleteMessage(OrdersCompleteEvent payload) {
        this.messageId = UUID.randomUUID().toString();
        this.publishedTime = System.currentTimeMillis();
        this.payload = payload;
    }
}
