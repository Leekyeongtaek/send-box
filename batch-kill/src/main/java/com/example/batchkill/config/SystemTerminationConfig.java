package com.example.batchkill.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
//@Configuration
public class SystemTerminationConfig {

    private AtomicInteger processesKilled = new AtomicInteger(0);
    private final int TERMINATION_TARGET = 5;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

//    @Bean
    public Job systemDestructionJob(Step systemDestructionStep) {
        return new JobBuilder("systemDestructionJob", jobRepository)
                .validator(new DefaultJobParametersValidator(
                        new String[]{"destructionPower"}, //필수
                        new String[]{"targetSystem"} //선택
                ))
                .start(systemDestructionStep)
                .build();
    }

//    @Bean
    public Job processTerminatorJob(JobRepository jobRepository, Step terminationStep) {
        return new JobBuilder("processTerminatorJob", jobRepository)
                .start(terminationStep)
                .build();
    }

//    @Bean
    public Step terminationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet terminatorTasklet) {
        return new StepBuilder("terminationStep", jobRepository)
                .tasklet(terminatorTasklet, transactionManager)
                .build();
    }

//    @Bean
//    @StepScope
    public Tasklet terminatorTasklet2(@Value("#{jobParameters}['infiltrationTargets']") String infiltrationTargets) {
        return ((contribution, chunkContext) -> {
            String[] targets = infiltrationTargets.split(",");
            log.info("⚡ 침투 작전 개시");
            log.info("첫 번째 타겟: {} 침투 시작", targets[0]);
            log.info("마지막 번째 타겟 {}에서 집결", targets[1]);
            log.info("🎯 임무 전달 완료");
            return RepeatStatus.FINISHED;
        });
    }

//    @Bean
//    @StepScope
    public Tasklet terminatorTasklet(
            @Value("#{jobParameters['terminatorId']}") String terminatorId,
            @Value("#{jobParameters['targetCount']}") Integer targetCount
    ) {
        return ((contribution, chunkContext) -> {
            log.info("시스템 종결자 정보:");
            log.info("ID: {}", terminatorId);
            log.info("제거 대상 수: {}", targetCount);
            log.info("⚡ SYSTEM TERMINATOR {} 작전을 개시합니다.", terminatorId);
            log.info("☠️ {}개의 프로세스를 종료합니다.", targetCount);

            for (int i = 1; i <= targetCount; i++) {
                log.info("💀 프로세스 {} 종료 완료!", i);
            }

            log.info("🎯 임무 완료: 모든 대상 프로세스가 종료되었습니다.");

            return RepeatStatus.FINISHED;
        });
    }

//    @Bean
    public Job systemTerminationSimulationJob() {
        return new JobBuilder("systemTerminationSimulationJob", jobRepository)
                .start(enterWorldStep())
                .next(meetNPCStep())
                .next(defeatProcessStep())
                .next(completeQuestStep())
                .build();
    }

//    @Bean
    public Step enterWorldStep() {
        return new StepBuilder("enterWorldStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("System Termination 시뮬레이션 세계에 접속했습니다!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

//    @Bean
    public Step meetNPCStep() {
        return new StepBuilder("meetNPCStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("시스템 관리자 NPC를 만났습니다.");
                    System.out.println("첫 번째 미션: 좀비 프로세스 " + TERMINATION_TARGET + "개 처형하기");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

//    @Bean
    public Step defeatProcessStep() {
        return new StepBuilder("defeatProcessStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int terminated = processesKilled.incrementAndGet();
                    System.out.println("좀비 프로세스 처형 완료! (현재 " + terminated + "/" + TERMINATION_TARGET + ")");
                    if (terminated < TERMINATION_TARGET) {
                        return RepeatStatus.CONTINUABLE;
                    } else {
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

//    @Bean
    public Step completeQuestStep() {
        return new StepBuilder("completeQuestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("미션 완료! 좀비 프로세스 " + TERMINATION_TARGET + "개 처형 성공");
                    System.out.println("보상: kill -9 권한 획득, 시스템 제어 레벨 1 달성");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
