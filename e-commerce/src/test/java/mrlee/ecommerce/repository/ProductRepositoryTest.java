package mrlee.ecommerce.repository;

import mrlee.ecommerce.domain.Product;
import mrlee.ecommerce.dto.ProductSearchCondition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void findAll() {
        Product productA = new Product("신라면", 1500);
        Product productB = new Product("안성탕면", 1000);
        Product productC = new Product("쫄병스낵", 800);

        productRepository.save(productA);
        productRepository.save(productB);
        productRepository.save(productC);

        ProductSearchCondition condition = new ProductSearchCondition();
        condition.setName("신라면");
        condition.setMinPrice(1500);

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Product> products = productRepository.searchPage(condition, pageRequest);

        assertThat(products.getTotalElements()).isEqualTo(1);
    }
}
