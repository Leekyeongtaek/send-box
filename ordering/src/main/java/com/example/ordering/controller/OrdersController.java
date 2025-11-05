package com.example.ordering.controller;

import com.example.ordering.dto.OrdersReqDto;
import com.example.ordering.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrdersReqDto ordersReqDto, @RequestHeader("X-User-Id") String userId) {
        ordersService.createOrders(ordersReqDto, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
