package com.example.ordering.domain;

import com.example.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders_fail_log")
@Entity
public class OrdersFailLog extends BaseTimeEntity {

    @Id
    @Column(name = "ORDERS_FAIL_LOG_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ordersId;

    private String failMessage;

    public OrdersFailLog(Long ordersId, String failMessage) {
        this.ordersId = ordersId;
        this.failMessage = failMessage;
    }
}
