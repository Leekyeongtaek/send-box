package com.example.batch.job.member;

import com.example.batch.code.MemberStatus;
import com.example.batch.domain.MemberBatch;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaMemberRegistrationConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    //./gradlew bootRun --args='--spring.batch.job.name=memberRegistrationJpaJob'

    @Bean
    public Job memberRegistrationJpaJob(Step memberRegistrationJpaStep) {
        return new JobBuilder("memberRegistrationJpaJob", jobRepository)
                .start(memberRegistrationJpaStep)
                .build();
    }

    @Bean
    public Step memberRegistrationJpaStep(
            JpaPagingItemReader<MemberBatch> reader,
            ItemProcessor<MemberBatch, MemberBatch> processor,
            JpaItemWriter<MemberBatch> writer
    ) {
        return new StepBuilder("memberRegistrationJpaStep", jobRepository)
                .<MemberBatch, MemberBatch>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public JpaCursorItemReader<MemberBatch> memberJpaCursorReader() {
        return new JpaCursorItemReaderBuilder<MemberBatch>()
                .name("memberJpaCursorReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT m
                        FROM MemberBatch m
                        WHERE m.requestedAt <= :requestedAt AND m.status = :status
                        ORDER BY m.id ASC
                        """)
                .parameterValues(Map.of(
                        "requestedAt", LocalDateTime.now(),
                        "status", MemberStatus.READY
                ))
                .build();
    }

    @Bean
    public JpaPagingItemReader<MemberBatch> memberJpaPagingReader() {
        return new JpaPagingItemReaderBuilder<MemberBatch>()
                .name("memberJpaPagingReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT m
                        FROM MemberBatch m
                        WHERE m.requestedAt <= :requestedAt AND m.status = :status
                        ORDER BY m.id ASC
                        """)
                .parameterValues(Map.of(
                        "requestedAt", LocalDateTime.now(),
                        "status", MemberStatus.READY
                ))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<MemberBatch, MemberBatch> memberItemJpaProcessor() {
        return member -> {
            member.done();
            return member;
        };
    }

    @Bean
    public JpaItemWriter<MemberBatch> memberJpaWriter() {
        return new JpaItemWriterBuilder<MemberBatch>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false) // 엔티티 저장 시 persist()를 사용할지 merge()를 사용할 지 여부 결정
                .build();
    }
}
