package com.example.batch.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders_item_batch")
@Entity
public class OrdersItemBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDERS_ITEM_BATCH_ID", nullable = false)
    private Long id;

    @Column(name = "PRODUCT_BATCH_ID", nullable = false)
    private Long productBatchId;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "PRICE", nullable = false)
    private int price;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ORDERS_BATCH_ID")
    private OrdersBatch ordersBatch;

    @Override
    public String toString() {
        return "OrdersItemBatch{" +
                "id=" + id +
                ", productBatchId=" + productBatchId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
