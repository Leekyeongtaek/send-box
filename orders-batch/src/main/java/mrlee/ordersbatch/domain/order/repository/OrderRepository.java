package mrlee.ordersbatch.domain.order.repository;

import mrlee.ordersbatch.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Modifying
    @Query(
            "update Order o set o.orderStatus = 'CONFIRM' " +
                    "where o.orderStatus = 'DELIVERED' " +
                    "and o.orderedAt between :startAt and :endAt")
    int updateAutoConfirm(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);
}
