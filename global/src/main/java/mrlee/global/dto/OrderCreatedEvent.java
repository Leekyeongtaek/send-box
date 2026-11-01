package mrlee.global.dto;

import lombok.Getter;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        Long productId,
        Integer quantity
) {
}
