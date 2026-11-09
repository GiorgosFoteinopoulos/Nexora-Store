package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository products;

    @InjectMocks
    private ProductService service;

    private static Phone phone() {
        Phone phone = new Phone("Nexora Phone 15", new Brand("Nexora"), "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku("NX-PH15-128-BLK"), "Black", 128, Money.euros("799"), 5));
        return phone;
    }

    @Test
    void returnsTheProductPage() {
        when(products.findBySlug(new Slug("nexora-phone-15"))).thenReturn(Optional.of(phone()));

        ProductResponse response = service.getProduct("nexora-phone-15");

        assertThat(response.name()).isEqualTo("Nexora Phone 15");
        assertThat(response.variants()).hasSize(1);
    }

    @Test
    void anUnknownSlugIsNotFound() {
        when(products.findBySlug(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProduct("nexora-phone-99"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No product with slug 'nexora-phone-99'");
    }
    @ParameterizedTest(name = "\"{0}\" is not found")
    @ValueSource(strings = {"Nexora_Phone", "UPPER-case", "with space"})
    void anInvalidSlugIsNotFoundWithoutAskingTheDatabase(String slug) {
        assertThatThrownBy(() -> service.getProduct(slug))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(products);
    }
}
