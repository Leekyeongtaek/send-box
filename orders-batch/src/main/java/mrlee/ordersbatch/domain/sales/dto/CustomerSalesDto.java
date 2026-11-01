package mrlee.ordersbatch.domain.sales.dto;

public record CustomerSalesDto(
        Long customerId,
        Long totalSalesAmount,
        Long orderCount
) {}
