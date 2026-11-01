package mrlee.ecommerce.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "e_orders_item")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int orderPrice;
    private int count;

    public static OrderItem createOrderItem(Order order, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.product = product;
        orderItem.orderPrice = product.getPrice();
        orderItem.count = 1;

        return orderItem;
    }

    public static OrderItem createOrderItem(Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        return orderItem;
    }
}
