package mrlee.orders.repository;

import mrlee.orders.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();

    @Transactional
    @Modifying
    @Query("UPDATE OutboxEvent o SET o.published = true WHERE o.id = :id")
    void markAsPublishedById(@Param("id")  Long id);
}
