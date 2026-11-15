package com.practice.nexora.catalog;

import com.jayway.jsonpath.JsonPath;
import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Money;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminVariantApiIntegrationTest {

    private static final String URL = "/api/admin/variants/NX-PH15-128-BLK";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Phone phone = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku("NX-PH15-128-BLK"), "Black", 128, Money.euros("799"), 10));
        products.save(phone);
        accounts.save(new UserAccount("admin@nexora.local", passwordEncoder.encode("admin-password"), Role.ADMIN));
        accounts.save(new UserAccount("george@example.com", passwordEncoder.encode("correct-horse"), Role.USER));
    }

    @AfterEach
    void cleanUp() {
        products.deleteAll();
        brands.deleteAll();
        accounts.deleteAll();
    }

    private String tokenFor(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        String answer = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(answer, "$.accessToken");
    }

    private static String patchBody(int stock, long version) {
        return """
                {"stock": %d, "version": %d}
                """.formatted(stock, version);
    }

    @Test
    void anAdminReadsThenChangesWithTheVersion() throws Exception {
        String admin = "Bearer " + tokenFor("admin@nexora.local", "admin-password");

        mvc.perform(get(URL).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.version").value(0));

        mvc.perform(patch(URL).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(patchBody(7, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void aChangeBasedOnAnOldVersionIs409() throws Exception {
        String admin = "Bearer " + tokenFor("admin@nexora.local", "admin-password");

        mvc.perform(patch(URL).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(patchBody(7, 0)))
                .andExpect(status().isOk());

        mvc.perform(patch(URL).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(patchBody(15, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void aNormalUserCannotChangeStock() throws Exception {
        String user = "Bearer " + tokenFor("george@example.com", "correct-horse");

        mvc.perform(patch(URL).header("Authorization", user)
                .contentType(MediaType.APPLICATION_JSON).content(patchBody(999, 0)))
                .andExpect(status().isForbidden());
    }
}
