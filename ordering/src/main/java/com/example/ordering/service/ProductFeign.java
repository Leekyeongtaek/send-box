package com.example.ordering.service;

import com.example.ordering.dto.ProductResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// FeignClient 라이브러리를 사용하는 HTTP 요청이 가능한 객체로 등록
// 스프링 빈 등록
@FeignClient(name = "product-service")
public interface ProductFeign {

    @GetMapping("/products/{productId}")
    ProductResDto getProductById(@PathVariable("productId") Long productId, @RequestHeader("X-User-Id") String userId);
}
