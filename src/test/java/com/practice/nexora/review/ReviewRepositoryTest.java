package com.practice.nexora.review;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.catalog.Product;
import com.practice.nexora.catalog.ProductRepository;
import com.practice.nexora.user.Role;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviews;

    @Autowired
    private ProductRepository products;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private TestEntityManager em;

    private Product phone;
    private Product otherProduct;

    @BeforeEach
    void pickTwoProducts() {
        List<Product> all = products.findAll(Sort.by("id"));
        phone = all.get(0);
        otherProduct = all.get(1);
    }

    private UserAccount user(String email) {
        return users.save(new UserAccount(email, "some-hash", Role.USER));
    }

    @Test
    void savesAReviewAndReadsItBack() {
        UserAccount george = user("george@example.com");
        Long id = reviews.save(new Review(phone, george, 5, " Great battery. ")).getId();
        em.flush();
        em.clear();

        Review loaded = reviews.findById(id).orElseThrow();

        assertThat(loaded.getRating()).isEqualTo(5);
        assertThat(loaded.getComment()).isEqualTo("Great battery.");
        assertThat(loaded.getAuthor().getEmail()).isEqualTo("george@example.com");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void oneReviewPerUserPerProduct() {
        UserAccount george = user("george@example.com");
        reviews.saveAndFlush(new Review(phone, george, 4, null));

        assertThat(reviews.existsByProductAndAuthor(phone, george)).isTrue();
        assertThat(reviews.existsByProductAndAuthor(otherProduct, george)).isFalse();

        assertThatThrownBy(() -> reviews.saveAndFlush(new Review(phone, george, 2, "changed my mind")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void theDatabaseComputesAverageAndCount() {
        reviews.save(new Review(phone, user("a@example.com"), 4, null));
        reviews.save(new Review(phone, user("b@example.com"), 5, null));
        em.flush();

        assertThat(reviews.averageRating(phone)).isEqualTo(4.5);
        assertThat(reviews.countByProduct(phone)).isEqualTo(2);

        assertThat(reviews.averageRating(otherProduct)).isNull();
        assertThat(reviews.countByProduct(otherProduct)).isZero();
    }

    @Test
    void theEntityRejectsARatingOutsideOneToFive() {
        UserAccount george = user("george@example.com");

        assertThatThrownBy(() -> new Review(phone, george, 6, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 5");
        assertThatThrownBy(() -> new Review(phone, george, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
