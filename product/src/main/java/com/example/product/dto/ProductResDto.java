package com.example.product.dto;

import com.example.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;

    public ProductResDto to(Product product) {
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
