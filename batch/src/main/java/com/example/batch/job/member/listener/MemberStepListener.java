package com.example.batch.job.member.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class MemberStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> Step 시작! {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(" >>> Step 완료! ReadCount   : {} WriteCount  : {}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount()
        );
        return stepExecution.getExitStatus();
    }
}
