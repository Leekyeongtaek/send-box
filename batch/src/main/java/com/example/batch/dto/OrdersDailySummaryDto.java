package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDailySummaryDto {
    private Long id;
    private LocalDate summaryDate;
    private int orderCount;
    private int itemCount;
    private int completedOrders;
    private int failedOrders;
    private int canceledOrders;
    private int completedAmount;
    private int failedAmount;
    private int canceledAmount;
}
