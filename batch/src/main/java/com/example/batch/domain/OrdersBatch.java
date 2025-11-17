package com.example.batch.domain;

import com.example.batch.code.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders_batch")
@Entity
public class OrdersBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDERS_BATCH_ID", nullable = false)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "ORDER_DATE_TIME", nullable = false)
    private LocalDateTime orderDateTime;

    @OneToMany(fetch = FetchType.EAGER)
    private List<OrdersItemBatch> ordersItemBatchList;
}
