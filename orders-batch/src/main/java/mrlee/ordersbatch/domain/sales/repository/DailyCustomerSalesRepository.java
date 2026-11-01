package mrlee.ordersbatch.domain.sales.repository;

import mrlee.ordersbatch.domain.sales.eneity.DailyCustomerSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DailyCustomerSalesRepository extends JpaRepository<DailyCustomerSales, Long> {

    // 멱등성 보장을 위한 기존 데이터 삭제 메서드
    @Modifying
    @Query("delete from DailyCustomerSales d where d.salesDate = :salesDate")
    void deleteBySalesDate(@Param("salesDate") LocalDate salesDate);
}
