package com.practice.nexora.notification;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.user.Role;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class PriceDropAlertsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PriceDropAlerts alerts;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    private long productId;
    private String productSlug;
    private long variantId;
    private long georgeId;

    @BeforeEach
    void setUp() {
        Map<String, Object> product = jdbc.queryForMap("select id, slug from products order by id limit 1");
        productId = ((Number) product.get("id")).longValue();
        productSlug = (String) product.get("slug");
        variantId = jdbc.queryForObject(
                "select id from product_variants where product_id = ? order by id limit 1", Long.class, productId
        );
        georgeId = users.save(new UserAccount("george@example.com", "some-hash", Role.USER)).getId();
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private void wishes(long userId, String whenSql) {
        jdbc.update("insert into wishlist_items (user_id, product_id, added_at) values (?, ?, " + whenSql + ")",
                userId, productId);
    }

    private void priceChange(String oldAmount, String newAmount, String whenSql) {
        jdbc.update("""
                insert into price_history
                (variant_id, old_price_amount, old_price_currency, new_price_amount, new_price_currency,
                changed_by, changed_at)
                values (?, ?::numeric, 'EUR', ?::numeric, 'EUR', 'admin@nexoral.local', %s)
                """.formatted(whenSql), variantId, oldAmount, newAmount);
    }

    private int runTheJob() {
        return alerts.createPriceDropNotifications(Instant.now().minus(Duration.ofDays(1)));
    }

    private static RequestPostProcessor as(String email) {
        return jwt().jwt(token -> token.subject(email));
    }

    @Test
    void aDropOnAWishedProductCreatesANotification() throws Exception {
        wishes(georgeId, "now() - interval '1 hour'");
        priceChange("899.00", "849.00", "now()");


        assertThat(runTheJob()).isEqualTo(1);
        assertThat(runTheJob()).isZero();
    }

    @Test
    void aPriceIncreaseCreatesNothing() {
        wishes(georgeId, "now() - interval '1 hour'");
        priceChange("849.00", "899.00", "now()");

        assertThat(runTheJob()).isZero();
    }

    @Test
    void aDropBeforeTheUserWishedItIsIgnored() {
        priceChange("899.00", "849.00", "now() - interval '2 hours'");
        wishes(georgeId, "now()");

        assertThat(runTheJob()).isZero();
    }

    @Test
    void onlyUsersWhoWishedTheProductAreNotified() throws Exception {
        users.save(new UserAccount("maria@example.com", "some-hash", Role.USER));
        wishes(georgeId, "now() - interval '1 hour'");
        priceChange("899.00", "849.00", "now()");

        runTheJob();

        mockMvc.perform(get("/api/me/notifications").with(as("maria@example.com")))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void markAllReadIsIdempotent() throws Exception {
        wishes(georgeId, "now() - interval '1 hour'");
        priceChange("899.00", "849.00", "now()");
        runTheJob();

        mockMvc.perform(put("/api/me/notifications/read").with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/me/notifications/read").with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/me/notifications").with(as("george@example.com")))
                .andExpect(jsonPath("$[0].read").value(true));


    }

    @Test
    void notificationsNeedAToken() throws Exception {
        mockMvc.perform(get("/api/me/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
