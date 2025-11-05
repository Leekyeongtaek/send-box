package com.example.ordering.domain;

import com.example.ordering.code.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
@Entity
public class Orders {

    @Id
    @Column(name = "ORDERS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "ORDER_DATE_TIME", nullable = false)
    private LocalDateTime orderDateTime;

    @OneToMany(mappedBy = "orders", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<OrdersItem> ordersItems = new ArrayList<>();

    public Orders(Long userId, List<OrdersItem> ordersItems) {
        this.userId = userId;
        this.status = OrderStatus.PENDING;
        this.orderDateTime = LocalDateTime.now();
        ordersItems.forEach(this::addOrdersItem);
    }

    public void addOrdersItem(OrdersItem ordersItem) {
        ordersItems.add(ordersItem);
        ordersItem.mappingOrders(this);
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void fail() {
        this.status = OrderStatus.FAILED;
    }
}
