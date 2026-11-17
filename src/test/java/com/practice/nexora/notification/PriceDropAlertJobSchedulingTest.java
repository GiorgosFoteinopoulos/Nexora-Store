package com.practice.nexora.notification;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.user.Role;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest(properties = {
        "nexora.alerts.enabled=true",
        "nexora.alerts.interval=PT1S",
        "nexora.alerts.initial-delay=PT1S"})
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class PriceDropAlertJobSchedulingTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserAccountRepository users;

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    @Test
    void theSchedulerFindsTheDropOnItsOwn() {
        long productId = jdbc.queryForObject("select id from products order by id limit 1", Long.class);
        long variantId = jdbc.queryForObject(
                "select id from product_variants where product_id = ? order by id limit 1", Long.class, productId);
        long georgeId = users.save(new UserAccount("george@example.com", "some-hash", Role.USER)).getId();

        jdbc.update("insert into wishlist_items (user_id, product_id, added_at) values (?, ?, now() - interval '1 hour')",
                georgeId, productId);
        jdbc.update("""
                insert into price_history
                    (variant_id, old_price_amount, old_price_currency, new_price_amount, new_price_currency,
                     changed_by, changed_at)
                values (?, 899.00, 'EUR', 849.00, 'EUR', 'admin@nexora.local', now())
                """, variantId);


        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(jdbc.queryForObject(
                        "select count(*) from notifications where user_id = ?", Long.class, georgeId))
                        .isEqualTo(1));
    }
}