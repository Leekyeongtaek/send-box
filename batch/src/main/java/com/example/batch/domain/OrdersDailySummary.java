package com.example.batch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders_daily_summary")
@Entity
public class OrdersDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDERS_DAILY_SUMMARY_ID", nullable = false)
    private Long id;

    @Column(name = "SUMMARY_DATE", nullable = false, unique = true)
    private LocalDate summaryDate;

    // JPA에서는 가급적 객체형(Integer, Long)을 쓰는 게 유연성 면에서 좋습니다. 하지만 성능상 큰 차이는 없고, 실제로 int도 잘 쓰입니다.
    @Column(name = "ORDER_COUNT", nullable = false)
    private int orderCount;

    @Column(name = "ITEM_COUNT", nullable = false)
    private int itemCount;

    @Column(name = "COMPLETED_ORDERS", nullable = false)
    private int completedOrders;

    @Column(name = "FAILED_ORDERS", nullable = false)
    private int failedOrders;

    @Column(name = "CANCELED_ORDERS", nullable = false)
    private int canceledOrders;

    @Column(name = "COMPLETED_AMOUNT", nullable = false)
    private int completedAmount;

    @Column(name = "FAILED_AMOUNT", nullable = false)
    private int failedAmount;

    @Column(name = "CANCELED_AMOUNT", nullable = false)
    private int canceledAmount;
}
