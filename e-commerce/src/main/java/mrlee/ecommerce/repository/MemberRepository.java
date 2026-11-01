package mrlee.ecommerce.repository;

import mrlee.ecommerce.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
