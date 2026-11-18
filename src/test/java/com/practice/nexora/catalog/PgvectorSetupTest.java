package com.practice.nexora.catalog;


import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class PgvectorSetupTest {

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    @Test
    void theVectorExtensionIsInstalled() {
        String version = jdbc.queryForObject(
                "select extversion from pg_extension where extname = 'vector'", String.class);

        assertThat(version).isNotBlank();
    }

    @Test
    void cosineDistanceMeasuresDirectionNotLength() {

        Double sameDirection = jdbc.queryForObject("select '[1,0,0]'::vector <=> '[5,0,0]'::vector", Double.class);

        Double unrelated = jdbc.queryForObject("select '[1,0,0]'::vector <=> '[0,1,0]'::vector", Double.class);

        assertThat(sameDirection).isEqualTo(0.0);
        assertThat(unrelated).isEqualTo(1.0);
    }

    @Test
    void aProductAcceptsOneVectorOfExactly1536Numbers() {
        long productId = jdbc.queryForObject("select id from products order by id limit 1", Long.class);

        jdbc.update("""
                insert into product_embeddings (product_id, embedding, content_hash, model, embedded_at)
                values (?, array_fill(0.01::real, array[1536])::vector, 'test-hash', 'test-model', now())
                """, productId);

        assertThat(jdbc.queryForObject(
                "select vector_dims(embedding) from product_embeddings where product_id = ?", Integer.class, productId))
                .isEqualTo(1536);

        assertThatThrownBy(() -> jdbc.update("""
                update product_embeddings set embedding = '[1,2,3]'::vector where product_id = ?
                """, productId))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1536");
    }
}
