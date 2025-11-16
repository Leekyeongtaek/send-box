package com.example.batchkill.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
//@Configuration
public class SystemTerminationConfig2 {

//    @Bean
    public Job systemTerminationJob2(JobRepository jobRepository, Step scanningStep, Step eliminationStep) {
        return new JobBuilder("systemTerminationJob2", jobRepository)
                .start(scanningStep)
                .next(eliminationStep)
                .build();
    }

//    @Bean
    public Step scanningStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager) {
        return new StepBuilder("scanningStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String target = "판교 서버실";
                    ExecutionContext stepContext = contribution.getStepExecution().getExecutionContext();
                    stepContext.put("targetSystem", target);
                    log.info("타겟 스캔 완료: {}", target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(promotionListener())
                .build();
    }

//    @Bean
    public Step eliminationStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                Tasklet eliminationTasklet) {
        return new StepBuilder("eliminationStep", jobRepository)
                .tasklet(eliminationTasklet, transactionManager)
                .build();
    }

//    @Bean
//    @StepScope
    public Tasklet eliminationTasklet(@Value("#{jobExecutionContext}[targetSystem]") String target) {
        return ((contribution, chunkContext) -> {
            log.info("시스템 제거 작업 실행: {}", target);
            return RepeatStatus.FINISHED;
        });
    }

//    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"targetSystem"});
        return listener;
    }

//    @Bean
    public Job killDashNineJob(JobRepository jobRepository, Step terminationStep2) {
        return new JobBuilder("killDashNineJob", jobRepository)
                .listener(systemTerminationListener(null))  // 파라미터는 런타임에 주입
                .start(terminationStep2)
                .build();
    }

//    @Bean
    public Step terminationStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("terminationStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("시스템 제거 프로토콜 실행 중...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

//    @Bean
//    @JobScope
    public JobExecutionListener systemTerminationListener(@Value("#{jobParameters['terminationType']}") String terminationType) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("시스템 제거 시작! 제거 방식: {}", terminationType);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("작전 종료! 시스템 상태: {}", jobExecution.getStatus());
            }
        };
    }
}
