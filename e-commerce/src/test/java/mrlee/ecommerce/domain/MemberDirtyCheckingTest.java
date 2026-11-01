package mrlee.ecommerce.domain;

import jakarta.persistence.EntityManager;
import mrlee.ecommerce.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberDirtyCheckingTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager em;

    @Test
    void dirtyCheckingTest() {
        Member member = new Member();
        member.changeName("초기 회원");
        memberRepository.save(member);

        em.flush();
        em.clear();

        System.out.println("================== 조회 시작 ==================");

        Member findMember = memberRepository.findById(member.getId()).orElseThrow();

        findMember.changeName("변경된 회원");

        em.flush();
    }
}
