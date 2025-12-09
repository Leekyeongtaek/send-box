package com.example.batch.repository;

import com.example.batch.domain.MemberBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBatchRepository extends JpaRepository<MemberBatch, Long> {
}
