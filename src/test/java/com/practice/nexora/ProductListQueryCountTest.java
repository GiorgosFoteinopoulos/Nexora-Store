package com.practice.nexora;

import com.practice.nexora.catalog.*;
import com.practice.nexora.common.Money;
import com.practice.nexora.common.PageResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ProductService.class})
class ProductListQueryCountTest {

    @Autowired
    private ProductService service;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private ProductRepository products;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private QueryCounter queries;

    @BeforeEach
    void startCounting() {
        queries = new QueryCounter(entityManagerFactory);
    }


    private void saveCatalogOf(int phones) {
        List<Brand> twoBrands = brands.saveAll(List.of(new Brand("Nexora"), new Brand("Voltix")));
        for (int i = 1; i <= phones; i++) {
            Phone phone = new Phone("Phone %02d".formatted(i), twoBrands.get(i % 2), "A phone.", 6.1, 4500, true);
            phone.addVariant(new ProductVariant(
                    new Sku("PH-%02d-128-BLK".formatted(i)), "Black", 128, Money.euros("499"), 5));
            products.save(phone);
        }
        entityManager.flush();
        entityManager.clear();
        queries.reset();
    }

    @ParameterizedTest(name = "{0} products cost 1 + 2 + {0} statements")
    @ValueSource(ints = {3, 6, 12})
    void theListCostsOneQueryPlusOnePerBrandPlusOnePerProduct(int n) {
        saveCatalogOf(n);

        PageResponse<ProductSummaryResponse> page = service.listProducts(ProductFilter.none(), PageRequest.of(0, 20));

        assertThat(page.content()).hasSize(n);
        assertThat(queries.entityFetches()).isEqualTo(2);
        assertThat(queries.collectionFetches()).isEqualTo(n);
        assertThat(queries.statements()).isEqualTo(1 + 2 + n);
    }

    @Test
    void theProductPageCostsAFixedThreeStatements() {
        saveCatalogOf(6);

        service.getProduct("phone-03");

        assertThat(queries.statements()).isEqualTo(3);
    }
}