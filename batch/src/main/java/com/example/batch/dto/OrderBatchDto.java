package com.example.batch.dto;

import com.example.batch.code.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBatchDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private LocalDateTime orderDateTime;
    private List<OrderItemBatchDto> ordersItems = new ArrayList<>();
}
