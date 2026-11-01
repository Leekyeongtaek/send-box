package mrlee.ordersbatch.job.sales;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.ordersbatch.domain.sales.dto.CustomerSalesDto;
import mrlee.ordersbatch.domain.sales.eneity.DailyCustomerSales;
import mrlee.ordersbatch.domain.sales.repository.DailyCustomerSalesRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.item.ChunkOrientedStep;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DailyCustomerSalesJpaConfiguration {

    private final DailyCustomerSalesRepository salesRepository;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job dailyCustomerSalesJpaJob(JobRepository jobRepository,
                                        Step deleteSalesStep,
                                        Step aggregateSalesStep) {
        return new JobBuilder("dailyCustomerSalesJpaJob", jobRepository)
                .start(deleteSalesStep) // 1. 기존 데이터 삭제 (멱등성 보장)
                .next(aggregateSalesStep) // 2. 집계 처리
                .build();
    }

    // STEP 1: 멱등성 보장을 위한 삭제 Tasklet Step
    @Bean
    public Step deleteSalesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteSalesStep", jobRepository).tasklet((contribution, chunkContext) -> {
            String targetDateStr = (String) chunkContext.getStepContext().getJobParameters().get("targetDate");
            LocalDate targetDate = LocalDate.parse(targetDateStr);
            log.info("[deleteSalesStep] {} 일자의 기존 집계 데이터를 삭제합니다.", targetDate);
            salesRepository.deleteBySalesDate(targetDate);

            return RepeatStatus.FINISHED;
        }, transactionManager).build();
    }

    // // STEP 2: 청크 기반 집계 Step
    @Bean
    public Step aggregateSalesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aggregateSalesStep", jobRepository)
                .<CustomerSalesDto, DailyCustomerSales>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerSalesJpaReader(null))
                .processor(customerSalesJpaProcessor(null))
                .writer(customerSalesJpaWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<CustomerSalesDto> customerSalesJpaReader(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {
        LocalDate date = LocalDate.parse(targetDate);
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = date.plusDays(1).atStartOfDay();

        Map<String, Object> params = new HashMap<>();
        params.put("startAt", startAt);
        params.put("endAt", endAt);

        // 핵심 학습 포인트: JpaPagingItemReader는 반드시 ORDER BY가 필요합니다!
        String jpql = "SELECT new mrlee.ordersbatch.domain.sales.dto.CustomerSalesDto(o.customerId, SUM(o.totalAmount), COUNT(o))" +
                "FROM Order o " +
                "WHERE o.orderedAt >= :startAt AND o.orderedAt <= :endAt " +
                "GROUP BY o.customerId " +
                "ORDER BY o.customerId ASC";

        return new JpaPagingItemReaderBuilder<CustomerSalesDto>()
                .name("customerSalesJpaReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(jpql)
                .parameterValues(params)
                .build();
    }

    @Bean
    public ItemProcessor<CustomerSalesDto, DailyCustomerSales> customerSalesJpaProcessor(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {

        return dto -> {
            log.info("Processor 실행 - customerId: {}", dto.customerId());
            return DailyCustomerSales.builder()
                    .salesDate(LocalDate.parse(targetDate))
                    .customerId(dto.customerId())
                    .totalSalesAmount(dto.totalSalesAmount())
                    .orderCount(dto.orderCount().intValue())
                    .build();
        };
    }

    @Bean
    public JpaItemWriter<DailyCustomerSales> customerSalesJpaWriter() {
        return new JpaItemWriterBuilder<DailyCustomerSales>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
