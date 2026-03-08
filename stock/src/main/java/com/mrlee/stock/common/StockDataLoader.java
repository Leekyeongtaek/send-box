package com.mrlee.stock.common;

import com.mrlee.stock.domain.Stock;
import com.mrlee.stock.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
//@Component
public class StockDataLoader implements CommandLineRunner {

    private final StockRepository stockRepository;

    public StockDataLoader(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("StockDataLoader 실행...");

        ClassPathResource resource = new ClassPathResource("my.csv");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        //log.info("파일명 = {}, 사이즈 = {}", resource.getFilename(), resource.contentLength());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<Stock> stocks = csvParser.getRecords().stream().map(record -> {
                String department = record.get("소속부");

                return Stock.builder()
                        .standardCode(record.get("표준코드"))
                        .shortCode(record.get("단축코드"))
                        .korName(record.get("한글 종목명"))
                        .korAbbrName(record.get("한글 종목약명"))
                        .engName(record.get("영문 종목명"))
                        .listedDate(LocalDate.parse(record.get("상장일"), dateTimeFormatter))
                        .marketType(record.get("시장구분"))
                        .securitiesType(record.get("증권구분"))
                        .department(department.isBlank() ? null : department)
                        .stockType(record.get("주식종류"))
                        .faceValue(parseToInt(record.get("액면가")))
                        .listedShares(Long.parseLong(record.get("상장주식수").replace(",", "")))
                        .build();
            }).toList();

            log.info("주식 사이즈 = {}", stocks.size());
//            stockRepository.saveAll(stocks);
        } catch (Exception e) {
            log.error("❌ 데이터 로딩 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    private int parseToInt(String value) {
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
