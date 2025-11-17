package com.example.batch.repository;

import com.example.batch.domain.OrdersDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersDailySummaryRepository extends JpaRepository<OrdersDailySummary, Long> {
}
