package com.example.common.events.orders;

import lombok.*;

import java.util.List;

/**
 * 주문이 생성되었을 때 발생하는 도메인 이벤트
 * - 상품 서비스에서 재고 차감 등의 처리를 위해 사용됨
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrdersCreateEvent {
    private String orderId;
    private List<OrderItem> orderItems;

    public List<Long> getProductIds() {
        return orderItems.stream()
                .map(OrderItem::getProductId)
                .map(Long::parseLong)
                .toList();
    }

    @Getter
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private int quantity;
    }
}
