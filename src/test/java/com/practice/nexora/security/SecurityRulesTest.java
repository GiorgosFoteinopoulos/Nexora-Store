package com.practice.nexora.security;


import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void theCatalogIsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/brands")).andExpect(status().isOk());
    }

    @Test
    void theDocsAndHealthCheckArePublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void changingTheCatalogNeedsALogin() throws Exception {
        mvc.perform(post("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void anyOtherUrlNeedsALogin() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }
}
