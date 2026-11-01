package mrlee.ecommerce.domain;

import jakarta.persistence.EntityManager;
import mrlee.ecommerce.repository.MemberRepository;
import mrlee.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SpringBootTest
public class NPlusOneTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager em;

    @Test
    void nPlusOneTest() {
        for (int i = 1; i < 4; i++) {
            Member member = new Member();
            member.changeName("회원" + i);
            memberRepository.save(member);

            Order order = Order.createOrder(member);
            orderRepository.save(order);
        }

        em.flush();
        em.clear();

        List<Order> orders = orderRepository.findAll();
        // class mrlee.ecommerce.domain.Member$HibernateProxy
        System.out.println(orders.get(0).getMember().getClass());
        for (Order order : orders) {
            System.out.println(order.getMember().getName());
        }
    }

    @Test
    void fetchJoinTest() {
        for (int i = 1; i < 4; i++) {
            Member member = new Member();
            member.changeName("회원" + i);
            memberRepository.save(member);

            Order order = Order.createOrder(member);
            orderRepository.save(order);
        }

        em.flush();
        em.clear();

        List<Order> orders = orderRepository.findAllWithMemberFetchJoin();

        //class mrlee.ecommerce.domain.Member
        System.out.println(orders.get(0).getMember().getClass());
        for (Order order : orders) {
            System.out.println(order.getMember().getName());
        }
    }
}
