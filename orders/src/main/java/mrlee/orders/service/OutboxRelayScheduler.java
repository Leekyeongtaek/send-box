package mrlee.orders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.orders.domain.OutboxEvent;
import mrlee.orders.producer.OrderProducer;
import mrlee.orders.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OrderProducer orderProducer;
    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedDelay = 100) //0.1초
    public void relayEvents() {
        List<OutboxEvent> unpublishedEvents = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : unpublishedEvents) {
            try {
                // 카프카 전송
                orderProducer.sendOrderCreatedEvent(event.getAggregateId(), event.getPayload()).join();
                // 전송 성공 시 상태 업데이트
                outboxEventRepository.markAsPublishedById(event.getId());
            } catch (Exception e) {
                // 실패한 경우 이번 배치에서는 넘어가고 다음 스케줄러 실행 시 재시도됨
                log.error("Failed to publish outbox event id: {}", event.getId(), e);
            }
        }
    }
}
