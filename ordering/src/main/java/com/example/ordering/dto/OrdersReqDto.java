package com.example.ordering.dto;

import com.example.ordering.domain.OrdersItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdersReqDto {

    private List<OrdersItemReqDto> ordersItems;

    public List<OrdersItem> toOrdersItems() {
        return ordersItems.stream()
                .map(OrdersReqDto.OrdersItemReqDto::toEntity)
                .toList();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersItemReqDto {
        private Long productId;
        private String productName;
        private int price;
        private int quantity;

        public OrdersItem toEntity() {
            return new OrdersItem(productId, productName, price, quantity);
        }
    }
}
