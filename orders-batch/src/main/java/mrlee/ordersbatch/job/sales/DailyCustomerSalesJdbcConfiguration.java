package mrlee.ordersbatch.job.sales;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.ordersbatch.domain.sales.dto.CustomerSalesDto;
import mrlee.ordersbatch.domain.sales.eneity.DailyCustomerSales;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyCustomerSalesJdbcConfiguration {

    private final DataSource dataSource; // JDBC 사용을 위한 DataSource 주입
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job dailyCustomerSalesJdbcJob(JobRepository jobRepository,
                                         Step deleteSalesStep,
                                         Step aggregateJdbcStep) {

        return new JobBuilder("dailyCustomerSalesJdbcJob", jobRepository)
                .start(deleteSalesStep)
                .next(aggregateJdbcStep)
                .build();
    }

    @Bean
    public Step aggregateJdbcStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteSalaggregateJdbcStepesStep", jobRepository)
                .<CustomerSalesDto, DailyCustomerSales>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerSalesJdbcReader(null))
                .processor(customerSalesJdbcProcessor(null))
                .writer(customerSalesJdbcWriter())
                .faultTolerant() // 결함 허용 설정 시작
                .skip(NullPointerException.class) // 건너뛸 예외 지정
                .skipLimit(10) // 해당 예외가 10번 발생할 때까지만 Skip 허용 (11번째엔 Job 실패)
                //.noSkip(DataAccessException.class) // DB 연결 오류 등 치명적 에러는 Skip하지 않고 즉시 실패 처리 가능
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<CustomerSalesDto> customerSalesJdbcReader(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {
        LocalDate date = LocalDate.parse(targetDate);
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = date.plusDays(1).atStartOfDay();

        String sql = "SELECT customer_id, SUM(total_amount) AS total_sales_amount, COUNT(order_id) AS order_count " +
                "FROM orders " +
                "WHERE ordered_at >= ? AND ordered_at < ? " +
                "GROUP BY  customer_id";

        return new JdbcCursorItemReaderBuilder<CustomerSalesDto>()
                .name("customerSalesJdbcReader")
                .dataSource(dataSource)
                .sql(sql)
                .queryArguments(startAt, endAt)
                .rowMapper((rs, rowNum) -> new CustomerSalesDto(
                        rs.getLong("customer_id"),
                        rs.getLong("total_sales_amount"),
                        rs.getLong("order_count")
                ))
                .fetchSize(CHUNK_SIZE) // 매우 중요! 4단계 OOM과 직결됨
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<CustomerSalesDto, DailyCustomerSales> customerSalesJdbcProcessor(
            @Value("#{jobParameters['targetDate']}") String targetDate) {
        return dto -> DailyCustomerSales.builder()
                .salesDate(LocalDate.parse(targetDate))
                .customerId(dto.customerId())
                .totalSalesAmount(dto.totalSalesAmount())
                .orderCount(dto.orderCount().intValue())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<DailyCustomerSales> customerSalesJdbcWriter() {
        // JPA 영속성 컨텍스트를 거치지 않고 바로 Bulk Insert 수행 (성능 극대화)
        String sql = "INSERT INTO daily_customer_sales (sales_date, customer_id, total_sales_amount, order_count) " +
                "VALUES (:salesDate, :customerId, :totalSalesAmount, :orderCount)";

        return new JdbcBatchItemWriterBuilder<DailyCustomerSales>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped() // DailyCustomerSales 객체의 필드명과 named parameter(:필드명) 자동 매핑
                .build();
    }
}
