package com.example.common.message.orders;

import com.example.common.events.orders.OrdersCreateEvent;
import lombok.*;

/**
 * Kafka로 송신되는 주문 생성 메시지
 * - 이벤트에 대한 메타정보(publishedTime, messageId 등) 포함
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdersCreateMessage {

    private String messageId;
    private Long publishedTime;
    private OrdersCreateEvent payload;
}
