package com.example.batch.dto;

import com.example.batch.code.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBatchDto {
    private Long id;
    private String email;
    private LocalDateTime requestedAt;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getStatusName() {
        return status != null ? status.name() : null;
    }
}
