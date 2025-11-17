package com.example.batch.job;

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
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrdersDailySummaryJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

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
    public JpaCursorItemReader<OrdersBatch> ordersDailySummaryReader(
            @Value("#{jobParameters['orderDate']}") String orderDate) {
        return null;
    }

    @Bean
    public JpaItemWriter<OrdersDailySummary> ordersDailySummaryWriter() {
        return new JpaItemWriterBuilder<OrdersDailySummary>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    @Component
    public static class OrdersDailySummaryProcessor implements ItemProcessor<OrdersBatch, OrdersDailySummary> {

        @Override
        public OrdersDailySummary process(OrdersBatch item) throws Exception {

            return null;
        }
    }
}
