package mrlee.ecommerce.domain;

import jakarta.persistence.EntityManager;
import mrlee.ecommerce.repository.MemberRepository;
import mrlee.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class FetchOomTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager em;

    @Test
    void fetchJoinWithPaginationOOMTest() {

    }
}
