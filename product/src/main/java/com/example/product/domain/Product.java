package com.example.product.domain;

import com.example.common.exception.CustomException;
import com.example.product.code.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "product")
@Entity
public class Product {

    @Id
    @Column(name = "PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "STOCK_QUANTITY", nullable = false)
    private Integer stockQuantity;

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    public void decreaseStockQuantity(int quantity) {
        int remainingStock = stockQuantity - quantity;

        if (remainingStock < 0) {
            throw new CustomException(id + "번 상품인 " + name + " 상품의 재고가 부족합니다. 현재 재고: " + stockQuantity);
        }

        this.stockQuantity = remainingStock;
    }
}
