# Send-Box 프로젝트
- MSA 아키텍처와 스프링 클라우드, 카프카를 활용해서 주문, 상품 도메인에 대한 프로젝트입니다.

## 기술 스택

### 백엔드
- Java 17
- SpringBoot 3.4.1
- JPA (Spring Data JPA, QueryDSL)
- MySql 8.0.32
- Docker
- Kafka

### 개발 도구
- IntelliJ IDEA

## 프로젝트 구조 이미지
![게임스토어 ERD](./images/game-store-erd.png)

### 테이블

#### 상품 테이블
|컬럼명|데이터 타입|제약 조건|설명|
|---|---|---|---|
|product_id|BIGINT|PK, AUTO_INCREMENT|상품 식별자|
|category|VARCHAR(50)|NOT NULL|상품 카테고리{RAMEN, SNACK, DRINK, SOUP}|
|name|VARCHAR(255)|NOT NULL, UNIQUE|상품의 이름|
|price|INT|NOT NULL|상품의 가격|
|stock_quantity|INT|NOT NULL|상품의 재고|

### 주요 기능 코드
