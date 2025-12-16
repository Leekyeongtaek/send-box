package com.example.batch.job.member;

import com.example.batch.code.MemberStatus;
import com.example.batch.common.MemberSkipException;
import com.example.batch.dto.MemberBatchDto;
import com.example.batch.job.DateUtil;
import com.example.batch.job.member.listener.MemberChunkListener;
import com.example.batch.job.member.listener.MemberJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepListener;
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
import org.springframework.dao.DataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpServerErrorException;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcMemberRegistrationConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private int cnt;

    //./gradlew bootRun --args='--spring.batch.job.name=memberRegistrationJob requestedAtStr=2025-12-14T23:59:59'

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

    //스텝 실행 중 단 하나의 예외라도 발생하면? -> 스탭 단위로 실행하고 그 중 하나에서 예외 발생시 전체 롤백
    //태스크릿 지향 처리는 Spring Batch 내결함성(FaultTolerance)기능의 지원 대상이 아니다.
    //재시도: 일시적 네트워크 오류
    //건너뛰기: 잘못된 형식의 데이터, 더 이상 재시도할 수 없을 때 건너뛰기가 작동된다는 것
    //내결함성 활성화 시 RetryTemplate 장착
    // - 내결함성 기능이 활성화되면 최초 실행부터 재시도까지 모든 시도가 이 retryCallback을 통해 수행
    // - RetryPolicy: SimpleRetryPolicy 기본 설정(발생한 예외가 사전에 지정된 예외 유형에 해당하는가, 현재 재시도 횟수가 최대 허용 횟수를 초과하지 않았는가)
    // - 충격적 진실: ItemReader? 재시도 따위는 없다. (Spring Batch가 mutable한 데이터소스로부터 데이터를 읽는 상황까지 고려했기 때문)
    //   - 메시지 큐(RabbitMQ, SQS 등)처럼 읽으면 사라지는 메시지
    @Bean
    public Step memberRegistrationStep(
            JdbcCursorItemReader<MemberBatchDto> reader,
            ItemProcessor<MemberBatchDto, MemberBatchDto> processor,
            JdbcBatchItemWriter<MemberBatchDto> writer
    ) {
        return new StepBuilder("memberRegistrationStep", jobRepository)
                // 최대 16KB(8192 char array)만큼의 데이터를 버퍼에 저장
                // chunk 단위로 트랜잭션 커밋
                .<MemberBatchDto, MemberBatchDto>chunk(5000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new MemberChunkListener())
                                // 내결함성(FaultTolerance) 기능을 사용할 땐 ItemProcessor가 멱등하게 동작해야 한다고 명시
                .faultTolerant()// 이제부터 재시도나 스킵 같은 내결함성 기능을 사용하겠다"고 선언하는 것
//                .retry(MemberException.class) // retry(), retryLimit() 을 사용자 정의 RetryPolicy와 동시에 사용하지 않는 것을 권장, AND 조건
//                .noRetry(IllegalArgumentException.class)
//                .retryLimit(3)
//                .processorNonTransactional() // ItemProcessor 비트랜잭션 처리, 2007부터 시작했으나 해당 옵션시 5000으로 끝남
                //.retryPolicy(new TimeoutRetryPolicy(Long.MAX_VALUE)) // CPU를 고문하는 무한 재시도 루프
                //.retryPolicy(new MaxAttemptsRetryPolicy(Integer.MAX_VALUE)) // 시스템이 뜨거워질 때까지 재시도 - 쿨러 멜트다운 확정
//                .backOffPolicy(new ExponentialBackOffPolicy() {{
//                    setInitialInterval(1000L);  // 초기 대기 시간
//                    setMultiplier(2.0);        // 대기 시간 증가 배수
//                    setMaxInterval(10000L);     // 최대 대기 시간
//                }})
//                .noRollback(NonFatalBusinessException.class)
                .skip(MemberSkipException.class) // LimitCheckingItemSkipPolicy
                .skipLimit(2) // skipLimit의 기본값은 10이다, 스텝 전체에 허용된 건너뛰기 횟수
                .build();
    }

    // 롤백이 발생할 때마다 매번 과거로 되돌아가 아이템을 다시 읽는 것일까?
    // Spring Batch ItemReader의 중요한 설계 원칙
    // - 'forward only' 방식이다. 즉, 데이터를 단방향으로만 순차적으로 읽어나가는 것이 기본 원칙이다.
    // 내결함성 기능이 활성화된 경우 ItemReader가 읽어들인 input Chunk를 별도로 저장해둔다.
    // - 재시도가 필요할 때 읽어둔 Chunk를 그대로 재사용하여 처리할 수 있는 것
    // ItemReader의 건너뛰기 메커니즘은 단순하다. Spring Batch Step은 read() 메서드 호출 중 예외가 발생하면, 이 예외를 catch하고 SkipPolicy를 사용해 해당 예외가 건너뛰기 가능한지 판단한다.
    // ItemReader에서의 건너뛰기는 청크 사이즈에 영향을 미치지 않는다.
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
                            mb.member_batch_id,
                            mb.requested_at,
                            mb.status,
                            mb.updated_at
                        FROM member_batch mb
                        WHERE mb.requested_at <= ? and mb.status = ?
                        ORDER BY mb.member_batch_id
                        """) // 쿼리는 딱 1번 실행
                .queryArguments(List.of(requestedAt, "READY"))
                .beanRowMapper(MemberBatchDto.class)
                .fetchSize(1000) // DB는 1000 rows 단위로 ResultSet을 전달, DB → JVM 메모리로 가져오는 단위 (커서 fetch size)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberBatchDto> memberJdbcPagingReader(
            @Value("#{jobParameters['requestedAtStr']}") String requestedAtStr) throws Exception {

        LocalDateTime requestedAt = DateUtil.parseToLocalDateTime(requestedAtStr);

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

    // ItemProcessor가 실행 중에 매번 생성·호출되는 객체가 아니라, 스프링 컨테이너가 한 번 생성해두는 “빈” 이라는 점
    // 아이템 처리 시점이 아니라 빈 생성 시점
    // 청크 전체가 다시 처리되지만, 재시도 횟수는 아이템 단위로 개별 관리된다.
    @Bean
    public ItemProcessor<MemberBatchDto, MemberBatchDto> memberItemProcessor() {

        //process() 내부에서 카운트 하는 방식 사용
        return member -> { // 5000번 실행
            cnt++;

            log.info("process cnt 카운트 : {}", cnt);

            // 재시도 시 2007부터 7007까지 전체 다시 재시작
            if (cnt == 2006) {
                //throw new MemberException("데이터베이스 네트워크 오류");
                throw new MemberSkipException("잘못된 형식의 데이터로 스킵 예외 발생"); // DONE: 4,999 개
            }

            member.setStatus(MemberStatus.DONE);
            member.setUpdatedAt(LocalDateTime.now());
            return member;
        };
    }

    // writer에 5000개 한번에 전달해서, 한 번만 실행
    // JDBC Batch Update 실행
    // 트랜잭션 커밋 1회, 그러나 스캔 모드가 발동되면 각 아이템을 하나씩 개별 처리한다.
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

    @Bean
    public RetryListener retryListener() {

        return new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                System.out.println("throwable : " + throwable + " (현재 총 시도 횟수 = " + context.getRetryCount() + ")... \n");
            }
        };
    }

    private RetryPolicy retryPolicy() {
        Map<Class<? extends Throwable>, RetryPolicy> policyMap = new HashMap<>();

        SimpleRetryPolicy dbRetryPolicy = new SimpleRetryPolicy(3);
        SimpleRetryPolicy apiRetryPolicy = new SimpleRetryPolicy(5);

        policyMap.put(DataAccessException.class, dbRetryPolicy);
        policyMap.put(HttpServerErrorException.class, apiRetryPolicy);

        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setPolicyMap(policyMap);

        return retryPolicy;
    }

    public interface SkipListener<T, S> extends StepListener {

        default void onSkipInRead(Throwable t) {
        }

        default void onSkipInWrite(S item, Throwable t) {
        }

        default void onSkipInProcess(T item, Throwable t) {
        }
    }
}
