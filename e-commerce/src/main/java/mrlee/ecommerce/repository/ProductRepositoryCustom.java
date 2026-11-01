package mrlee.ecommerce.repository;

import mrlee.ecommerce.domain.Product;
import mrlee.ecommerce.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> searchPage(ProductSearchCondition condition, Pageable pageable);
}
