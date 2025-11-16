package com.example.batchkill.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;

@Slf4j
public class ItemProcessExampleConfig2 {

    @Data
    public static class SystemLog {
        private Long userId;      // 실행한 사용자
        private String rawCommand;  // 원본 명령어
        private LocalDateTime executedAt; // 실행 시간
    }

    @Data
    public static class CommandReport {
        private Long executorId;    // 처리된 사용자 ID
        private String action;      // 처리된 행동 설명
        private String severity;    // 위험 등급
        private LocalDateTime timestamp; // 실행 시간
    }


    public static class CommandAnalyzer implements ItemProcessor<SystemLog, CommandReport> {

        @Override
        public CommandReport process(SystemLog systemLog) throws Exception {

            CommandReport report = new CommandReport();
            report.setExecutorId(systemLog.getUserId());
            report.setTimestamp(systemLog.getExecutedAt());

            if (systemLog.getRawCommand().contains("rm")) {
                report.setAction("시스템 파일 제거 시도");
                report.setSeverity("CRITICAL");
            }

            return report;
        }
    }
}
