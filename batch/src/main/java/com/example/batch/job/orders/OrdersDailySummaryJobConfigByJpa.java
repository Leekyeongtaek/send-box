package com.example.batch.job.orders;

import com.example.batch.domain.OrdersBatch;
import com.example.batch.domain.OrdersDailySummary;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.database.builder.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrdersDailySummaryJobConfigByJpa {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    //./gradlew bootRun --args='--spring.batch.job.name=ordersDailySummaryJob orderDate=2025-11-17'

    @Bean
    public Job ordersDailySummaryJob(Step ordersDailySummaryStep) {
        return new JobBuilder("ordersDailySummaryJob", jobRepository)
                .start(ordersDailySummaryStep)
                .build();
    }

    @Bean
    public Step ordersDailySummaryStep(
            JpaCursorItemReader<OrdersBatch> reader,
            OrdersDailySummaryProcessor processor,
            JpaItemWriter<OrdersDailySummary> writer) {
        return new StepBuilder("ordersDailySummaryStep", jobRepository)
                .<OrdersBatch, OrdersDailySummary>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope // Reader Bean이 StepScope라서 잡 실행 시점의 JobParameter start/end가 주입됨
    public JpaCursorItemReader<OrdersBatch> jpaCursorReader(
            @Value("#{jobParameters['orderDate']}") LocalDate orderDate) {

        LocalDateTime startOrderDate = orderDate.atStartOfDay();
        LocalDateTime endOrderDate = orderDate.atTime(23, 59, 59);

        return new JpaCursorItemReaderBuilder<OrdersBatch>()
                .name("jpaCursorReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT ob
                        FROM OrdersBatch ob JOIN FETCH ob.ordersItems
                        WHERE ob.orderDateTime BETWEEN :startOrderDate AND :endOrderDate
                        ORDER BY ob.id
                        """)
                .parameterValues(
                        Map.of("startOrderDate", startOrderDate, "endOrderDate", endOrderDate))
                .build();
    }

    public JpaPagingItemReader<OrdersBatch> jpaPagingReader
            (@Value("#{jobParameters['orderDate']}") LocalDate orderDate) {
        return null;
    }

    @Bean
    public JpaItemWriter<OrdersDailySummary> jpaItemWriter() {
        return new JpaItemWriterBuilder<OrdersDailySummary>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true) // Set whether the entity manager should perform a persist instead of a merge.
                .build();
    }

    @Component
    public static class OrdersDailySummaryProcessor implements ItemProcessor<OrdersBatch, OrdersDailySummary> {

        @Override
        public OrdersDailySummary process(OrdersBatch item) throws Exception {
            log.info("Processing OrdersBatch : {}", item);
            return null;
        }
    }
}
