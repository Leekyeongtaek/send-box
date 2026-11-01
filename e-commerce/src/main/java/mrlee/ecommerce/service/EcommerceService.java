package mrlee.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.ecommerce.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EcommerceService {

    // JVM 메모리 누수 실습용 정적 리스트
    private static final List<Member> memoryLeakList = new ArrayList<>();
    private final Random random = new Random();

    // 1. RED 지표 실습: 랜덤 API 지연 및 에러 발생
    public void createOrderWithRandomDelay() {
        log.info("Creating order with random delay...");
        try {
            int delay = random.nextInt(500) + 100; // 100~600ms 지연, (0.1~0.6초)
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 20% 확률로 예외 발생 (500 에러 유도)
        if (random.nextInt(10) < 2) {
            throw new RuntimeException("e-commerce 모듈 타임아웃 발생 (테스트 에러)");
        }
        log.info("주문 처리 완료");
    }

    // 2. HikariCP 고갈 실습: 트랜잭션 점유 상태로 긴 시간 대기
    public void holdDatabaseConnection() {
        log.info("Holding database connection...");
        try {
            // @Transactional 내에서 스레드를 멈추면 DB 커넥션을 반환하지 않고 계속 쥐고 있음
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("DB 커넥션 반환 완료");
    }

    // 3. JVM Heap 메모리 고갈 실습
    public void generateMemoryLeak() {
        log.info("Generating memory leak...");
        for (int i = 0; i < 30000; i++) {
            // Member 엔티티를 계속 생성하여 static 리스트에 적재 (가비지 컬렉터가 회수 못함)
            memoryLeakList.add(Member.builder().name("Dummy Member " + i).build());
        }
        log.info("현재 메모리 누수 리스트 크기: {}", memoryLeakList.size());
    }

    // 4. CPU 스파이크 실습
    public void spikeCpuLoad() {
        log.info("Spike CPU Load...");
        double dummyResult = 0;
        for (int i = 0; i < 10000000; i++) {
            dummyResult += Math.sin(i) * Math.cos(i); // 무의미한 수학 연산으로 CPU 점유
        }
        log.info("CPU 연산 완료: {}", dummyResult);
    }
}
