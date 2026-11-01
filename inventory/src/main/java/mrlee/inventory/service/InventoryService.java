package mrlee.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.inventory.domain.ProcessedEvent;
import mrlee.inventory.repository.ProcessedEventRepository;
import mrlee.inventory.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private final ProcessedEventRepository processedEventRepository;
    private final StockRepository stockRepository;

    public void decreaseStock(Long orderId, Long productId) {
        String eventId = "INVENTORY_ORDER_" + orderId;

        if ((productId % 2) == 0) {
            log.info("(productId % 2) == 0 IllegalArgumentException...");
            throw new IllegalArgumentException("not found product id");
        }

        // 1. 중복 처리 검증
        //todo 여기까지 온 경우 메시지는 어떻게 되지? 그리고 중복으로 어떻게 여기까지 왔지?
//        if (processedEventRepository.existsById(eventId)) {
//            log.info("이미 처리된 주문입니다. (중복 메시지 무시) orderId: {}", orderId);
//            return;
//        }

        log.info("상품 {}의 재고를 차감합니다.", productId);
        //processedEventRepository.save(new ProcessedEvent(eventId));
    }

}
