package com.example.batch.job.practice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SampleConfig {

    // ./gradlew bootRun --args='--spring.batch.job.name=sampleJob date=2025-11-16'

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job sampleJob(Step sampleStep) {
        return new JobBuilder("sampleJob", jobRepository)
                .start(sampleStep)
                .build();
    }

    @Bean
    public Step sampleStep(Tasklet sampleTasklet) {
        return new StepBuilder("sampleStep", jobRepository)
                .tasklet(sampleTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet sampleTasklet(@Value("#{jobParameters['date']}") String date) {
        return (contribution, chunkContext) -> {
            log.info("Success! Job Parameter date = {}", date);
            return RepeatStatus.FINISHED;
        };
    }
}
