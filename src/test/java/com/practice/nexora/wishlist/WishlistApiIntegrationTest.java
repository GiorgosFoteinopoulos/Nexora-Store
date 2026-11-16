package com.practice.nexora.wishlist;


import com.jayway.jsonpath.JsonPath;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class WishlistApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    private String first;
    private String second;


    @BeforeEach
    void pickTwoProductSlugs() throws Exception {
        String json = mockMvc.perform(get("/api/products").param("size", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        first = JsonPath.read(json, "$.content[0].slug");
        second = JsonPath.read(json, "$.content[1].slug");
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private RequestPostProcessor as(String email) {
        if (users.findByEmail(email).isEmpty()) {
            users.save(new UserAccount(email, "some-hash", Role.USER));
        }
        return jwt().jwt(token -> token.subject(email));
    }

    @Test
    void myWishlistStartsEmpty() throws Exception {
        mockMvc.perform(get("/api/me/wishlist").with(as("george@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addedProductsAppearNewestFirst() throws Exception {
        mockMvc.perform(put("/api/me/wishlist/{slug}", first).with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/me/wishlist/{slug}", second).with(as("george@example.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/wishlist").with(as("george@example.com")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].slug").value(second))
                .andExpect(jsonPath("$[1].slug").value(first));
    }

    @Test
    void addingTwiceKeepsOneItem_idempotent() throws Exception {
        mockMvc.perform(put("/api/me/wishlist/{slug}", first).with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/me/wishlist/{slug}", first).with(as("george@example.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/wishlist").with(as("george@example.com")))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void removingTwiceIsFine_idempotent() throws Exception {
        mockMvc.perform(put("/api/me/wishlist/{slug}", first).with(as("george@example.com")));

        mockMvc.perform(delete("/api/me/wishlist/{slug}", first).with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/me/wishlist/{slug}", first).with(as("george@example.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/wishlist").with(as("george@example.com")))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void everyoneSeesOnlyTheirOwnWishlist() throws Exception {
        mockMvc.perform(put("/api/me/wishlist/{slug}", first).with(as("george@example.com")));
        mockMvc.perform(put("/api/me/wishlist/{slug}", second).with(as("maria@example.com")));

        mockMvc.perform(get("/api/me/wishlist").with(as("george@example.com")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].slug").value(first));
        mockMvc.perform(get("/api/me/wishlist").with(as("maria@example.com")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].slug").value(second));
    }

    @Test
    void withoutATokenItIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/me/wihslist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addingAnUnknownProductIsNotFound() throws Exception {
        mockMvc.perform(put("/api/me/wishlist/{slug}", "no-such-product").with(as("george@example.com")))
                .andExpect(status().isNotFound());
    }
}
