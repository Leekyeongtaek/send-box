package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemBatchDto {
    private Long id;
    private Long ordersBatchId;
    private Long productBatchId;
    private String productName;
    private int price;
    private int quantity;
}
