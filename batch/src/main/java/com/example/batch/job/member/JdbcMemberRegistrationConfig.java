package com.example.batch.job.member;

import com.example.batch.code.MemberStatus;
import com.example.batch.dto.MemberBatchDto;
import com.example.batch.job.DateUtil;
import com.example.batch.job.member.listener.MemberChunkListener;
import com.example.batch.job.member.listener.MemberJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcMemberRegistrationConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    //./gradlew bootRun --args='--spring.batch.job.name=memberRegistrationJob requestedAtStr=2025-12-13T23:59:59'

    // 이런 코드에 현혹되지 마라
    //@Override
    //public void beforeJob(JobExecution jobExecution) {
    //    jobExecution.getExecutionContext()
    //        .put("targetDate", LocalDate.now()); // 치명적인 실수다
    //}

    //->JobParameter
    // 리스너는 감시와 통제만 담당

    @Bean
    public Job memberRegistrationJob(Step memberRegistrationStep) {
        return new JobBuilder("memberRegistrationJob", jobRepository)
                .start(memberRegistrationStep)
                .listener(new MemberJobListener())
                .build();
    }

    @Bean
    public Step memberRegistrationStep(
            JdbcPagingItemReader<MemberBatchDto> reader,
            ItemProcessor<MemberBatchDto, MemberBatchDto> processor,
            JdbcBatchItemWriter<MemberBatchDto> writer
    ) {
        return new StepBuilder("memberRegistrationStep", jobRepository)
                .<MemberBatchDto, MemberBatchDto>chunk(5000, transactionManager) // 최대 16KB(8192 char array)만큼의 데이터를 버퍼에 저장
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new MemberChunkListener())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MemberBatchDto> memberJdbcCursorReader(
            @Value("#{jobParameters['requestedAtStr']}") String requestedAtStr) {

        LocalDateTime requestedAt = DateUtil.parseToLocalDateTime(requestedAtStr);

        return new JdbcCursorItemReaderBuilder<MemberBatchDto>()
                .name("memberJdbcCursorReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT
                            mb.member_batch_id as id,
                            mb.requested_at,
                            mb.status,
                            mb.updated_at
                        FROM member_batch mb
                        WHERE mb.requested_at <= ? and mb.status = ?
                        ORDER BY mb.member_batch_id
                        """)
                .queryArguments(List.of(requestedAt, "READY"))
                .beanRowMapper(MemberBatchDto.class)
                .fetchSize(5000)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberBatchDto> memberJdbcPagingReader(
            @Value("#{jobParameters['requestedAtStr']}") String requestedAtStr) throws Exception {

        LocalDateTime requestedAt = DateUtil.parseToLocalDateTime(requestedAtStr);
        log.info("requestedAt = {}", requestedAt);
        return new JdbcPagingItemReaderBuilder<MemberBatchDto>()
                .name("memberJdbcPagingReader")
                .dataSource(dataSource)
                .pageSize(5000)
                .queryProvider(pagingQueryProvider(dataSource)) // 커스텀 PagingQueryProvider 적용
                .parameterValues(Map.of("status", "READY", "requestedAt", requestedAt))
                .beanRowMapper(MemberBatchDto.class)
                .build();
    }

    private PagingQueryProvider pagingQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProviderFactory = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactory.setDataSource(dataSource); // 데이터베이스 타입에 맞는 적절한 PagingQueryProvider 구현체를 생성할 수 있도록 dataSource를 전달해줘야 한다.

        queryProviderFactory.setSelectClause("SELECT member_batch_id, requested_at, status, updated_at");
        queryProviderFactory.setFromClause("FROM member_batch");
        queryProviderFactory.setWhereClause("WHERE requested_at <= :requestedAt AND member_batch.status = :status");
        queryProviderFactory.setSortKeys(Map.of("member_batch_id", Order.ASCENDING));

        return queryProviderFactory.getObject();
    }

    @Bean
    public ItemProcessor<MemberBatchDto, MemberBatchDto> memberItemProcessor() {
        return member -> {
            member.setStatus(MemberStatus.DONE);
            member.setUpdatedAt(LocalDateTime.now());
            return member;
        };
    }

    @Bean
    public JdbcBatchItemWriter<MemberBatchDto> memberJdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<MemberBatchDto>()
                .dataSource(dataSource)
                .sql("""
                            UPDATE member_batch
                            SET status = :statusName,
                                updated_at = :updatedAt
                            WHERE member_batch_id = :memberBatchId
                        """)
                .beanMapped()
                .assertUpdates(true)
                .build();
    }
}
