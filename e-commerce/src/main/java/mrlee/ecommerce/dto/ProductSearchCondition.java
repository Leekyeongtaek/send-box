package mrlee.ecommerce.dto;

import lombok.Data;

@Data
public class ProductSearchCondition {
    private String name;
    private Integer minPrice;
}
