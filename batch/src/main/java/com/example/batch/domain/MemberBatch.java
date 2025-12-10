package com.example.batch.domain;

import com.example.batch.code.MemberStatus;
import com.example.batch.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "member_batch")
@Entity
public class MemberBatch extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_BATCH_ID", nullable = false)
    private Long id;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "REQUESTED_AT", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    public void done() {
        this.status = MemberStatus.DONE;
    }
}
