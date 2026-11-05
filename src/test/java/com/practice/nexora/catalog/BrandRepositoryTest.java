package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // keep the real PostgreSQL
@Import(TestcontainersConfiguration.class)
class BrandRepositoryTest {

    @Autowired
    private BrandRepository brands;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void eachTestStartsWithAnEmptyTable() {
        assertThat(brands.count()).isZero();
    }

    @Test
    void savingGivesTheBrandAnId() {
        Brand brand = new Brand("Nexora");
        assertThat(brand.getId()).isNull(); // not saved yet

        Brand saved = brands.save(brand);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void readsTheBrandBackFromTheDatabase() {
        brands.save(new Brand("Nexora Labs"));
        entityManager.flush(); // send any pending SQL now
        entityManager.clear(); // forget cached objects: the next read must build a fresh one from the row

        Brand found = brands.findBySlug(new Slug("nexora-labs")).orElseThrow();

        assertThat(found.getName()).isEqualTo("Nexora Labs");
        assertThat(found.getSlug()).isEqualTo(new Slug("nexora-labs"));
        assertThat(brands.existsBySlug(new Slug("nexora-labs"))).isTrue();
    }

    @Test
    void theDatabaseRejectsTwoBrandsWithTheSameSlug() {
        brands.saveAndFlush(new Brand("Nexora"));

        assertThatThrownBy(() -> brands.saveAndFlush(new Brand("NEXORA"))) // same slug: "nexora"
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}