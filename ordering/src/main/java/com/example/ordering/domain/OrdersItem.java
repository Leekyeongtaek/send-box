package com.example.ordering.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders_item")
@Entity
public class OrdersItem {

    @Id
    @Column(name = "ORDERS_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName; //의도적 반정규화

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDERS_ID", nullable = false)
    private Orders orders;

    public OrdersItem(Long productId, String productName, Integer price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public void mappingOrders(Orders  orders) {
        this.orders = orders;
    }
}
