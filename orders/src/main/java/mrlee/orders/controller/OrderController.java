package mrlee.orders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.global.dto.OrderCreatedEvent;
import mrlee.orders.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderCreatedEvent orderCreatedEvent) {
        orderService.createOrder(orderCreatedEvent);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/test/orders")
    public String startLoadTest() {
        log.info("Start load test");
        new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                orderService.createOrder(new OrderCreatedEvent(null, 1L, BigDecimal.valueOf(120000), 1L, 1));
                try {
                    Thread.sleep(90);
                } catch (InterruptedException e) {}
            }
        }).start();
        return "초당 10건 부하 테스트 시작!";
    }
}
