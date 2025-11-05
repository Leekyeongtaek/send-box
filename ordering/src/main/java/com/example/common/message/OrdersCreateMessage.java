package com.example.common.message;

import com.example.common.events.OrdersCreateEvent;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka로 송신되는 주문 생성 메시지
 * - 이벤트에 대한 메타정보(publishedTime, messageId 등) 포함
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdersCreateMessage {

    private String messageId; // 메시지 고유 ID (추적용)
    private Long publishedTime; // 메시지 발행 시각
    private OrdersCreateEvent payload; // 실제 이벤트 데이터

    @Builder
    public OrdersCreateMessage(OrdersCreateEvent event) {
        this.messageId = UUID.randomUUID().toString();
        this.publishedTime = System.currentTimeMillis();
        this.payload = event;
    }
}
