package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class SchemaIndexTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void variantsAreIndexedByProduct() {
        @SuppressWarnings("unchecked")
        List<String> indexes = entityManager.getEntityManager()
                .createNativeQuery("select indexname from pg_indexes where tablename = 'product_variants'")
                .getResultList();

        assertThat(indexes).contains("ix_product_variants_product_id");
    }
}