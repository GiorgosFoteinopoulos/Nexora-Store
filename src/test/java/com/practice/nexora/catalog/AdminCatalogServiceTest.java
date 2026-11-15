package com.practice.nexora.catalog;


import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.ConflictException;
import com.practice.nexora.common.Money;
import com.practice.nexora.common.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, AdminCatalogService.class})
class AdminCatalogServiceTest {

    private static final String SKU = "NX-PH15-128-BLK";

    @Autowired
    private AdminCatalogService service;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private ProductRepository products;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceChangeRepository priceChanges;

    @BeforeEach
    void saveAPhone() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Phone phone = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku(SKU), "Black", 128, Money.euros("799"), 10));
        products.save(phone);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void aNewVariantStartsAtVersionZero() {
        assertThat(service.getVariant(SKU).version()).isZero();
    }

    @Test
    void anUpdateChangesTheValuesAndBumpsTheVersion() {
        AdminVariantResponse updated = service.updateVariant(SKU, new UpdateVariantRequest(new BigDecimal("849"), 7, 0L));

        assertThat(updated.price().amount()).isEqualTo("849.00");
        assertThat(updated.stock()).isEqualTo(7);
        assertThat(updated.version()).isEqualTo(1);
    }

    @Test
    void fieldsThatAreNotSentStayAsTheyAre() {
        AdminVariantResponse updated = service.updateVariant(SKU, new UpdateVariantRequest(null, 3, 0L));

        assertThat(updated.price().amount()).isEqualTo("799.00");
        assertThat(updated.stock()).isEqualTo(3);
    }

    @Test
    void anOldVersionIsAConflict() {
        service.updateVariant(SKU, new UpdateVariantRequest(null, 7, 0L));

        assertThatThrownBy(() -> service.updateVariant(SKU, new UpdateVariantRequest(null, 15, 0L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("now version 1, you sent 0");
    }

    @Test
    void anUnknownSkuIsNotFound() {
        assertThatThrownBy(() -> service.getVariant("NX-NOPE-1")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void aPriceChangeLeavesOneHistoryRow() {
        service.updateVariant(SKU, new UpdateVariantRequest(new BigDecimal("849"), null, 0L));

        List<PriceChange> history = priceChanges.findByVariant_SkuOrderByChangedAtAscIdAsc(new Sku(SKU));
        assertThat(history).singleElement().satisfies(change -> {
            assertThat(change.getOldPrice()).isEqualTo(Money.euros("799"));
            assertThat(change.getNewPrice()).isEqualTo(Money.euros("849"));
            assertThat(change.getChangedBy()).isEqualTo("system");
        });
    }

    @Test
    void aStockOnlyChangeLeavesNoHistory() {
        service.updateVariant(SKU, new UpdateVariantRequest(null, 3, 0L));

        assertThat(priceChanges.countByVariant_Sku(new Sku(SKU))).isZero();
    }

    @Test
    void theSamePriceAgainLeavesNoHistory() {
        service.updateVariant(SKU, new UpdateVariantRequest(new BigDecimal("799"), null, 0L));

        assertThat(priceChanges.countByVariant_Sku(new Sku(SKU))).isZero();
    }
}
