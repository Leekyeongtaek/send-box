package com.example.batch.job.member.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;

@Slf4j
public class MemberChunkListener implements ChunkListener {

    private long startTime;
    private int chunkCount = 0;

    @Override
    public void beforeChunk(ChunkContext context) {
        startTime = System.currentTimeMillis();
        chunkCount++;

        log.info(">>> [Chunk 시작] StepName={} ChunkIteration={}",
                context.getStepContext().getStepExecution().getStepName(),
                chunkCount
        );
    }

    @Override
    public void afterChunk(ChunkContext context) {
        long duration = System.currentTimeMillis() - startTime;

        StepExecution stepExecution = context.getStepContext().getStepExecution();

        log.info(">>> [Chunk 종료] ReadCount  : {} WriteCount : {} CommitCount : {} Duration : {} ms",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount(),
                duration);
    }

//    @Override
//    public void afterChunkError(ChunkContext context) {
//        ChunkListener.super.afterChunkError(context);
//    }
}
