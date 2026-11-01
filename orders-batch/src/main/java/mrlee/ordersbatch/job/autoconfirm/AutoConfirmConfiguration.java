package mrlee.ordersbatch.job.autoconfirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.ordersbatch.domain.order.repository.OrderRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AutoConfirmConfiguration {

    private final OrderRepository orderRepository;

    @Bean
    public Job autoConfirmJob(JobRepository jobRepository, Step autoConfirmStep) {
        return new JobBuilder("autoConfirmJob", jobRepository)
                .start(autoConfirmStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step autoConfirmStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("autoConfirmStep", jobRepository)
                .tasklet(autoConfirmTasklet(null), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet autoConfirmTasklet(@Value("#{jobParameters['requestDate']}") String requestDate) {
        return (contribution, chunkContext) -> {
            log.info("autoConfirmTasklet Tasklet Started...");
            log.info("autoConfirmTasklet requestDate: {}", requestDate);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
            String ctxRequestDate = String.valueOf(jobParameters.get("requestDate"));
            log.info("autoConfirmTasklet ctxRequestDate: {}", ctxRequestDate);

            LocalDate localDate = LocalDate.parse(requestDate);
            LocalDateTime startAt = localDate.minusDays(7).atStartOfDay();
            LocalDateTime endAt = localDate.atTime(LocalTime.MAX);

            int updateAutoConfirmCnt = orderRepository.updateAutoConfirm(startAt, endAt);
            stopWatch.stop();

            log.info("autoConfirmTasklet updateAutoConfirmCnt: {}", updateAutoConfirmCnt);
            log.info("Tasklet Running Time: {} ms", stopWatch.getTotalTimeMillis());

            return RepeatStatus.FINISHED;
        };
    }
}
