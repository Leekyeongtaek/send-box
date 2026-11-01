package mrlee.ordersbatch.domain.sales.eneity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Table(name = "daily_customer_sales")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCustomerSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    private LocalDate salesDate;
    private Long customerId;
    private Long totalSalesAmount;
    private Integer orderCount;

    @Builder
    public DailyCustomerSales(LocalDate salesDate, Long customerId, Long totalSalesAmount, Integer orderCount) {
        this.salesDate = salesDate;
        this.customerId = customerId;
        this.totalSalesAmount = totalSalesAmount;
        this.orderCount = orderCount;
    }
}
