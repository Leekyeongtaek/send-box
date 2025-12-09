package com.example.batch.dto;

import com.example.batch.code.MemberSource;
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
    private MemberSource source;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
