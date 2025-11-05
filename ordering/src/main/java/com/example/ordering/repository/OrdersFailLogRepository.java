package com.example.ordering.repository;

import com.example.ordering.domain.OrdersFailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersFailLogRepository extends JpaRepository<OrdersFailLog, Long> {
}
