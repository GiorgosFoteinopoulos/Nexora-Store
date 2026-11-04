package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTest {

    @Test
    void derivesItsSlugFromTheName() {
        Brand brand = new Brand("  Nexora Labs ");

        assertThat(brand.getName()).isEqualTo("Nexora Labs");
        assertThat(brand.getSlug()).isEqualTo(new Slug("nexora-labs"));
    }

    @Test
    void namesWithTheSameSlugAreTheSameBrand() {
        assertThat(new Brand("Nexora")).isEqualTo(new Brand("NEXORA"));
        assertThat(new Brand("Nexora").hashCode()).isEqualTo(new Brand("NEXORA").hashCode());
    }

    @Test
    void differentNamesAreDifferentBrands() {
        assertThat(new Brand("Nexora")).isNotEqualTo(new Brand("Voltix"));
    }

    @ParameterizedTest(name = "rejects \"{0}\"")
    @ValueSource(strings = {"", "   "})
    void rejectsBlankNames(String name) {
        assertThatThrownBy(() -> new Brand(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("brand name must not be blank");
    }

    @Test
    void rejectsNamesThatCannotBecomeASlug() {
        assertThatThrownBy(() -> new Brand("Κινητό"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot build a slug");
    }
}