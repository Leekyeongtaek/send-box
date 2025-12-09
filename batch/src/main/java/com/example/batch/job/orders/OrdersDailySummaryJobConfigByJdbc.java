package com.example.batch.job.orders;

import com.example.batch.domain.OrdersDailySummary;
import com.example.batch.dto.OrderBatchDto;
import com.example.batch.dto.OrderItemBatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrdersDailySummaryJobConfigByJdbc {

    // ordersDailySummaryJdbcJob
    // ordersDailySummaryJdbcStep
    // @Value("#{jobParameters['orderDate']}") LocalDate orderDate
    // LocalDateTime startOrderDate = orderDate.atStartOfDay();
    // LocalDateTime endOrderDate = orderDate.atTime(23, 59, 59);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    //./gradlew bootRun --args='--spring.batch.job.name=ordersDailySummaryJdbcJob orderDate=2025-11-16'

    @Bean
    public Job ordersDailySummaryJdbcJob(Step ordersDailySummaryJdbcStep) {
        return new JobBuilder("ordersDailySummaryJdbcJob", jobRepository)
                .start(ordersDailySummaryJdbcStep)
                .build();
    }

    @Bean
    public Step ordersDailySummaryJdbcStep(
            JdbcCursorItemReader<OrderBatchDto> reader,
            OrdersDailySummaryJdbcProcessor processor,
            OrdersDailySummaryWriter writer) {
        return new StepBuilder("ordersDailySummaryJdbcStep", jobRepository)
                .<OrderBatchDto, OrderBatchDto>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    //todo useCursorFetch=true 속성
    @Bean
    @StepScope
    public JdbcCursorItemReader<OrderBatchDto> jdbcCursorReader(
            @Value("#{jobParameters['orderDate']}") LocalDate orderDate) {

        LocalDateTime startOrderDate = orderDate.atStartOfDay();
        LocalDateTime endOrderDate = orderDate.atTime(23, 59, 59);

        return new JdbcCursorItemReaderBuilder<OrderBatchDto>()
                .name("jdbcCursorReader")
                .dataSource(dataSource)
                .sql("""
                            SELECT
                                ob.ORDERS_BATCH_ID as id,
                                ob.USER_ID,
                                ob.STATUS,
                                ob.ORDER_DATE_TIME
                            FROM orders_batch ob
                            WHERE ob.ORDER_DATE_TIME BETWEEN ? AND ?
                            ORDER BY ob.ORDERS_BATCH_ID
                        """)
                .queryArguments(List.of(startOrderDate, endOrderDate))
                .beanRowMapper(OrderBatchDto.class) // BeanPropertyRowMapper(Setter), DataClassRowMapper(Record), Custom RowMapper
                .fetchSize(10) //todo  chunk size와 page size를 일치시키는 것을 권장
                .build();
    }

    @Component
    @StepScope
    public static class OrdersDailySummaryJdbcProcessor implements ItemProcessor<OrderBatchDto, OrderBatchDto> {
        private final JdbcTemplate jdbcTemplate;

        public OrdersDailySummaryJdbcProcessor(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public OrderBatchDto process(OrderBatchDto item) {

            List<OrderItemBatchDto> items = jdbcTemplate.query(
                    """
                            SELECT
                                oib.ORDERS_ITEM_BATCH_ID AS id,
                                oib.ORDERS_BATCH_ID,
                                oib.PRODUCT_BATCH_ID,
                                oib.PRODUCT_NAME,
                                oib.PRICE,
                                oib.QUANTITY
                            FROM orders_item_batch oib
                            WEHRE ORDERS_BATCH_ID = ?
                            """,
                    new BeanPropertyRowMapper<>(OrderItemBatchDto.class),
                    item.getId()
            );

            item.setOrdersItems(items);

            return item;
        }
    }


    public static class OrdersDailySummaryWriter implements ItemWriter<OrderBatchDto> {

        private final JdbcTemplate jdbcTemplate;

        // 누적용 변수
        private int orderCount;
        private int itemCount;

        private int completedOrders;
        private int failedOrders;
        private int canceledOrders;

        private int completedAmount;
        private int failedAmount;
        private int canceledAmount;

        private LocalDate summaryDate;

        public OrdersDailySummaryWriter(JdbcTemplate jdbcTemplate,
                                        @Value("#{jobParameters['orderDate']}") LocalDate summaryDate) {
            this.jdbcTemplate = jdbcTemplate;
            this.summaryDate = summaryDate;
        }

        @Override
        public void write(Chunk<? extends OrderBatchDto> chunk) {
            for (OrderBatchDto order : chunk) {
                orderCount++;
                itemCount += order.getOrdersItems().size();

                int orderAmount = order.getOrdersItems().stream()
                        .mapToInt(i -> i.getPrice() * i.getQuantity())
                        .sum();

                switch (order.getStatus()) {
                    case COMPLETED -> {
                        completedOrders++;
                        completedAmount += orderAmount;
                    }
                    case FAILED -> {
                        failedOrders++;
                        failedAmount += orderAmount;
                    }
                    case CANCELLED -> {
                        canceledOrders++;
                        canceledAmount += orderAmount;
                    }
                }
            }
        }
    }

//    @Bean
//    @StepScope
//    public ItemReader<OrderBatchDto> orderBatchGroupingReader(JdbcCursorItemReader<OrderBatchJoinDto> jdbcDelegateReader) {
//        // 위에서 만든 GroupingReader로 감싸서 반환
//        return new OrderBatchGroupingReader(jdbcDelegateReader);
//    }

    //return new JdbcPagingItemReaderBuilder<Victim>()
    //            .name("terminatedVictimReader")
    //            .dataSource(dataSource)
    //            .pageSize(5)
    //            .selectClause("SELECT id, name, process_id, terminated_at, status")
    //            .fromClause("FROM victims")
    //            .whereClause("WHERE status = :status AND terminated_at <= :terminatedAt")
    //            .sortKeys(Map.of("id", Order.ASCENDING))
    //            .parameterValues(Map.of(
    //                    "status", "TERMINATED",
    //                    "terminatedAt", LocalDateTime.now()
    //            ))
    //            .beanRowMapper(Victim.class)
    //            .build();

    public JdbcPagingItemReader<OrderBatchDto> jdbcPagingReader(
            @Value("#{jobParameters['orderDate']}") LocalDate orderDate) {

        LocalDateTime startOrderDate = orderDate.atStartOfDay();
        LocalDateTime endOrderDate = orderDate.atTime(23, 59, 59);

        return new JdbcPagingItemReaderBuilder<OrderBatchDto>()
                .name("jdbcPagingReader")
                .dataSource(dataSource)
                .pageSize(10)
                .selectClause("")
                .fromClause("")
                .whereClause("")
                .sortKeys(Map.of())
                .parameterValues(Map.of("orderDate", startOrderDate, "endDate", endOrderDate))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<OrdersDailySummary> jdbcBatchWriter() {
        return new JdbcBatchItemWriterBuilder<OrdersDailySummary>()
                .dataSource(dataSource)
                .sql("") // UPDATE orders SET status = :status WHERE id = :id
                .beanMapped() // OrdersDailySummary 객체의 필드를 SQL 파라미터에 자동으로 매핑한다. Order.id 필드값이 :id에 바인딩된다.
                .assertUpdates(true) // true: 단 하나의 데이터라도 업데이트(또는 추가)에 실패하면 즉시 예외를 던져 작전을 중단한다.
                .build();
    }

}
