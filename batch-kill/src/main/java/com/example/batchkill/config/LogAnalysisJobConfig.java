package com.example.batchkill.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.RegexLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
//@Configuration
public class LogAnalysisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

//    @Bean
    public Job logAnalysisJob(Step logAnalysisStep) {
        return new JobBuilder("logAnalysisJob", jobRepository)
                .start(logAnalysisStep)
                .build();
    }

//    @Bean
    public Step logAnalysisStep(
            FlatFileItemReader<LogEntity> logItemReader,
            ItemWriter<LogEntity> logItemWriter
    ) {
        return new StepBuilder("logAnalysisStep", jobRepository)
                .<LogEntity, LogEntity>chunk(10, transactionManager)
                .reader(logItemReader)
                .writer(logItemWriter)
                .build();
    }

//    @Bean
//    @StepScope
    public ItemReader<LogEntity> logItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        RegexLineTokenizer tokenizer = new RegexLineTokenizer();
        tokenizer.setRegex("\\[\\w+\\]\\[Thread-(\\d+)\\]\\[CPU: \\d+%\\] (.+)");

        return new FlatFileItemReaderBuilder<LogEntity>()
                .name("logItemReader")
                .resource(new FileSystemResource(inputFile))
                .lineTokenizer(tokenizer)
                .fieldSetMapper(fieldSet -> new LogEntity(fieldSet.readString(0), fieldSet.readString(1)))
                .build();
    }

//    @Bean
    public ItemWriter<LogEntity> logItemWriter() {
        return items -> {
            for (LogEntity logEntity : items) {
                log.info(String.format("THD-%s: %s"),
                        logEntity.getThreadNum(), logEntity.getMessage());
            }
        };
    }

    @Data
    @AllArgsConstructor
    public static class LogEntity {
        private String threadNum;
        private String message;
    }
}
