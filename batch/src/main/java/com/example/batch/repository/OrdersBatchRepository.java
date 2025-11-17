package com.example.batch.repository;

import com.example.batch.domain.OrdersBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersBatchRepository extends JpaRepository<OrdersBatch, Long> {
}
