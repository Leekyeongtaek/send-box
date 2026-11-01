package mrlee.myoperation.repository;

import mrlee.myoperation.domain.MyOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyOperationRepository extends JpaRepository<MyOperation, Long> {
}
