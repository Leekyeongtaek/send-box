package com.example.ordering.service;

import com.example.common.message.OrdersCreateMessage;
import com.example.ordering.domain.Orders;
import com.example.ordering.domain.OrdersItem;
import com.example.ordering.dto.OrdersReqDto;
import com.example.ordering.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrdersEventMapper ordersEventMapper;
    //private final RestTemplate restTemplate;
    //private final ProductFeign productFeign; // @EnableFeignClients 필요

    //Jackson 직렬화에 문제 없도록 @NoArgsConstructor 유지, <도메인>.<이벤트명>-<action>
    //데이터 정합성 Validation 생략
    public void createOrders(OrdersReqDto ordersReqDto, String userId) {
        List<OrdersItem> ordersItems = ordersReqDto.toOrdersItems(); // 1. 주문 생성
        Orders orders = ordersRepository.save(new Orders(Long.valueOf(userId), ordersItems)); // 2. 주문 저장
        OrdersCreateMessage message = ordersEventMapper.toOrdersCreateEvent(orders.getId(), ordersReqDto); // 3. 이벤트 메시지 생성
        kafkaTemplate.send("ordering.orders-created", message); // 4. 카프카 메시지 발행
    }
}
