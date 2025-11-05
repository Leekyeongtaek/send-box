package com.example.ordering.dto;

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
}
