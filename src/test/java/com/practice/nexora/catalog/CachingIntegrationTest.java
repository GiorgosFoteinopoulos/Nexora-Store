package com.practice.nexora.catalog;


import com.jayway.jsonpath.JsonPath;
import com.practice.nexora.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.cache.type=caffeine")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class CachingIntegrationTest {

    private static final String NEW_PRICE = "1234.56";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JdbcTemplate jdbc;

    private Statistics statistics;
    private String slug;

    @BeforeEach
    void setUp() throws Exception {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        String json = mockMvc.perform(get("/api/products").param("size", "1"))
                .andReturn().getResponse().getContentAsString();
        slug = JsonPath.read(json, "$.content[0].slug");
    }

    @AfterEach
    void cleanUp() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }
    private long statementsFor(String url) throws Exception {
        statistics.clear();
        mockMvc.perform(get(url)).andExpect(status().is2xxSuccessful());
        return statistics.getPrepareStatementCount();
    }

    @Test
    void theSecondProductPageRequestNeverTouchesTheDatabase() throws Exception {
        String url = "/api/products/" + slug;

        assertThat(statementsFor(url)).isPositive();
        assertThat(statementsFor(url)).isZero();
    }

    @Test
    void theBrandListIsCachedToo() throws Exception {
        assertThat(statementsFor("/api/brands")).isPositive();
        assertThat(statementsFor("/api/brands")).isZero();
    }

    @Test
    void notFoundIsNeverCached() throws Exception {
        for (int attempt = 1; attempt <= 2; attempt++) {
            statistics.clear();
            mockMvc.perform(get("/api/products/no-such-product")).andExpect(status().isNotFound());
            assertThat(statistics.getPrepareStatementCount())
                    .as("attempt %d must ask the database", attempt)
                    .isPositive();
        }
    }

    @Test
    void anAdminPriceChangeTearsUpTheNotes() throws Exception {
        String url = "/api/products/" + slug;


        mockMvc.perform(get(url))
                .andExpect(content().string(not(containsString(NEW_PRICE))));


        String sku = jdbc.queryForObject("""
                select v.sku from product_variants v
                join products p on p.id = v.product_id
                where p.slug = ?
                order by v.id
                limit 1
                """, String.class, slug);
        var asAdmin = jwt().jwt(token -> token.subject("admin@nexora.local"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String variantJson = mockMvc.perform(get("/api/admin/variants/{sku}", sku).with(asAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number version = JsonPath.read(variantJson, "$.version");

        mockMvc.perform(patch("/api/admin/variants/{sku}", sku).with(asAdmin)
                        .contentType(APPLICATION_JSON)
                        .content("{\"price\": %s, \"version\": %d}".formatted(NEW_PRICE, version.longValue())))
                .andExpect(status().isOk());


        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(NEW_PRICE)));
    }
}
