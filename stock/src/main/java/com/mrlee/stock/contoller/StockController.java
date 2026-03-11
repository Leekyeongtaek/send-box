package com.mrlee.stock.contoller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/stocks")
@RestController
public class StockController {

    @GetMapping("/version")
    public ResponseEntity<String> checkAppVersion() {
        String appVersion = "현재 종목 앱 1.0.2v";
        return new ResponseEntity<>(appVersion, HttpStatus.OK);
    }
}
