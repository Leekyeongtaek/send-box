package mrlee.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.ecommerce.service.EcommerceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/e-commerce")
@RequiredArgsConstructor
public class EcommerceController {

    private final EcommerceService ecommerceService;

    @PostMapping("/orders")
    public String createOrder() {
        ecommerceService.createOrderWithRandomDelay();
        return "Order Created";
    }

    @GetMapping("/slow-query")
    public String slowQuery() {
        ecommerceService.holdDatabaseConnection();
        return "Slow Query Finished";
    }

    @GetMapping("/memory-leak")
    public String memoryLeak() {
        ecommerceService.generateMemoryLeak();
        return "Memory Leak Triggered";
    }

    @GetMapping("/cpu-load")
    public String cpuLoad() {
        ecommerceService.spikeCpuLoad();
        return "CPU Spiked";
    }
}
