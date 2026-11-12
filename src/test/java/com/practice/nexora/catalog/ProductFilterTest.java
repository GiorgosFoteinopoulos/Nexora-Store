package com.practice.nexora.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFilterTest {

    @Test
    void blankTextMeanNoFilter() {
        ProductFilter filter = new ProductFilter(null, " ", null, null, "");

        assertThat(filter.brand()).isNull();
        assertThat(filter.q()).isNull();
    }

    @Test
    void textsAreStripped() {
        ProductFilter filter = new ProductFilter(null, " nexora ", null, null, " pro ");

        assertThat(filter.brand()).isEqualTo("nexora");
        assertThat(filter.q()).isEqualTo("pro");
    }
}
