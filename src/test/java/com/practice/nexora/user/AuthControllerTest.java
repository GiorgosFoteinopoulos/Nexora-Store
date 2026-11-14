package com.practice.nexora.user;

import com.practice.nexora.common.ConflictException;
import com.practice.nexora.common.UnauthorizedException;
import com.practice.nexora.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    private static String body(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    @Test
    void registeringAnswers201WithoutThePassword() throws Exception {
        when(registrationService.register(any())).thenReturn(new UserResponse("george@example.com", Role.USER));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("george@example.com", "correct-horse")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("george@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void anInvalidEmailIsA400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("not-an-email", "correct-horse")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrationService);
    }

    @Test
    void aDuplicateEmailIsA409() throws Exception {
        when(registrationService.register(any()))
                .thenThrow(new ConflictException("An account with this email already exists"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("george@example.com", "correct-horse")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void aCorrectLoginReturnsAToken() throws Exception {
        when(loginService.login(any())).thenReturn(new TokenResponse("a.b.c", "Bearer", 3600));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("george@example.com", "correct-horse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("a.b.c"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void aWrongLoginIsA401() throws Exception {
        when(loginService.login(any())).thenThrow(new UnauthorizedException("Wrong email or password"));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("george@example.com", "wrong-horse")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Wrong email or password"));
    }
}
