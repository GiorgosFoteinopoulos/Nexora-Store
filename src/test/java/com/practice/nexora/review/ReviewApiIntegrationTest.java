package com.practice.nexora.review;

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
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class ReviewApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    private String slug;

    @BeforeEach
    void pickAProductSlugFromTheCatalog() throws Exception {
        String json = mockMvc.perform(get("/api/products").param("size", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        slug = JsonPath.read(json, "$.content[0].slug");
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }


    private ResultActions postReviewAs(String email, String body) throws Exception {
        if (users.findByEmail(email).isEmpty()) {
            users.save(new UserAccount(email, "some-hash", Role.USER));
        }
        return mockMvc.perform(post("/api/products/{slug}/reviews", slug)
                .with(jwt().jwt(token -> token.subject(email)))
                .contentType(APPLICATION_JSON)
                .content(body));
    }

    @Test
    void anyoneCanReadReviews_noRatingYet() throws Exception {
        mockMvc.perform(get("/api/products/{slug}/reviews", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.reviews").isEmpty());
    }

    @Test
    void aLoggedInUserWritesAReview_andEveryoneSeesIt() throws Exception {
        postReviewAs("george@example.com", """
                {"rating": 5, "comment": "Great battery."}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.author").value("g***"));

        mockMvc.perform(get("/api/products/{slug}/reviews", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.reviews[0].comment").value("Great battery."))
                .andExpect(jsonPath("$.reviews[0].author").value("g***"));
    }

    @Test
    void theSameUserCannotReviewTheSameProductTwice() throws Exception {
        postReviewAs("george@example.com", """
                {"rating": 4}
                """).andExpect(status().isCreated());

        postReviewAs("george@example.com", """
                {"rating": 1}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ReviewService.ALREADY_REVIEWED));
    }

    @Test
    void theAverageIsRoundedToOneDecimal() throws Exception {
        postReviewAs("a@example.com", "{\"rating\": 5}").andExpect(status().isCreated());
        postReviewAs("b@example.com", "{\"rating\": 4}").andExpect(status().isCreated());
        postReviewAs("c@example.com", "{\"rating\": 4}").andExpect(status().isCreated());

        mockMvc.perform(get("/api/products/{slug}/reviews", slug))
                .andExpect(jsonPath("$.reviewCount").value(3))
                .andExpect(jsonPath("$.averageRating").value(4.3)); // 13 / 3 = 4.333...
    }

    @Test
    void anonymousUsersCannotWrite() throws Exception {
        mockMvc.perform(post("/api/products/{slug}/reviews", slug)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rating\": 5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aRatingOutsideOneToFiveIsABadRequest() throws Exception {
        postReviewAs("george@example.com", "{\"rating\": 6}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewingAnUnknownProductIsNotFound() throws Exception {
        slug = "no-such-product";
        postReviewAs("george@example.com", "{\"rating\": 5}")
                .andExpect(status().isNotFound());
    }
}