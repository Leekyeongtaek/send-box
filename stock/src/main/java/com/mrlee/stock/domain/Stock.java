package com.mrlee.stock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long id;
    private String standardCode;
    private String shortCode;
    private String korName;
    private String korAbbrName;
    private String engName;
    private LocalDate listedDate;
    private String marketType;
    private String securitiesType;
    private String department;
    private String stockType;
    private int faceValue;
    private long listedShares;

    @Builder
    public Stock(String standardCode, String shortCode, String korName, String korAbbrName, String engName, LocalDate listedDate, String marketType, String securitiesType, String department, String stockType, int faceValue, long listedShares) {
        this.standardCode = standardCode;
        this.shortCode = shortCode;
        this.korName = korName;
        this.korAbbrName = korAbbrName;
        this.engName = engName;
        this.listedDate = listedDate;
        this.marketType = marketType;
        this.securitiesType = securitiesType;
        this.department = department;
        this.stockType = stockType;
        this.faceValue = faceValue;
        this.listedShares = listedShares;
    }
}
