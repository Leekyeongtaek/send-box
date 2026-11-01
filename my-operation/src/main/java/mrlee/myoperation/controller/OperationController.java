package mrlee.myoperation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/operation")
public class OperationController {

    private static final List<byte[]> leakBox = new ArrayList<>();

    @PostMapping("/health")
    public String healthCheck() {
        return "I am healthy! V2";
    }

    @GetMapping("/oom")
    public String startLeak() {
        new Thread(() -> {
            try {
                System.out.println("🚨 5분 타이머 OOM 폭탄 가동 시작!");
                while (true) {
                    // 2초마다 1MB씩 힙 메모리에 적재 (스프링 기본 구동 용량을 제외하고 약 5분 뒤 256MB 초과로 폭발)
                    leakBox.add(new byte[1024 * 1024]);
                    System.out.println("현재 메모리 누수량: " + leakBox.size() + " MB");
                    Thread.sleep(2000);
                }
            } catch (InterruptedException | OutOfMemoryError e) {
                System.err.println("💥 OOM 발생! 서버가 사망합니다: " + e.getMessage());
            }
        }).start();

        return "OOM 시한폭탄이 가동되었습니다. 도커 로그를 지켜보세요!";
    }
}
