package mrlee.orders.repository;

import mrlee.orders.domain.OutboxEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("페이징 없이 전체 조회하여 OOM 발생시키기")
    void triggerOom() {
        System.out.println("데이터 조회를 시작합니다...");
        List<OutboxEvent> allEvents = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();
        System.out.println("조회된 데이터 수: " + allEvents.size());
    }
}