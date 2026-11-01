package mrlee.ecommerce.domain;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import mrlee.ecommerce.repository.MemberRepository;
import mrlee.ecommerce.repository.OrderItemRepository;
import mrlee.ecommerce.repository.OrderRepository;
import mrlee.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
@Transactional
public class BatchFetchSizeTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    EntityManager em;

    @Test
    void batchFetchSizeTest() {
        Product productA = new Product("상품A", 1000);
        Product productB = new Product("상품B", 2000);
        productRepository.save(productA);
        productRepository.save(productB);

        //주문 2개를 만들고, 각 주문당 상품을 2개씩(총 4개) 추가
        for (int i = 1; i <= 2; i++) {
            Member member = new Member();
            member.changeName("회원" + i);
            memberRepository.save(member);

            Order order = Order.createOrder(member);
            orderRepository.save(order);

            OrderItem orderItem1 = OrderItem.createOrderItem(order, productA);
            OrderItem orderItem2 = OrderItem.createOrderItem(order, productB);

            orderItemRepository.save(orderItem1);
            orderItemRepository.save(orderItem2);
        }

        em.flush();
        em.clear();

        List<Order> orders = orderRepository.findAllWithMemberFetchJoin();

        for (Order order : orders) {
            log.info("주문번호 : {}", order.getId());
            for (OrderItem orderItem : order.getOrderItems()) {
                log.info("주문 상품 가격 : {}", orderItem.getOrderPrice());
            }
        }
    }
}
