package mrlee.ecommerce.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import mrlee.ecommerce.domain.Product;
import mrlee.ecommerce.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static mrlee.ecommerce.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchPage(ProductSearchCondition condition, Pageable pageable) {
        List<Product> products = queryFactory.selectFrom(product)
                .where(
                        nameContains(condition.getName()),
                        priceGoe(condition.getMinPrice()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        nameContains(condition.getName()),
                        priceGoe(condition.getMinPrice())
                );

        return PageableExecutionUtils.getPage(products, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? product.name.contains(name) : null;
    }

    private BooleanExpression priceGoe(Integer minPrice) {
        // >=
        return minPrice != null ? product.price.goe(minPrice) : null;
    }
}
