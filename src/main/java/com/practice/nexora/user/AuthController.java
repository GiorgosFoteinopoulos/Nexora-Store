package com.practice.nexora.user;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Accounts", description = "Register and (from 5.3) log in")
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;

    AuthController(RegistrationService registrationService, LoginService loginService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @Operation(summary = "Create an account")
    @ApiResponse(responseCode = "201", description = "Account created", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Invalid email or password",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "This email already has an account",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }

    @Operation(summary = "Log in and get a token")
    @ApiResponse(responseCode = "200", description = "The token to send as 'Authorization: Bearer ...'", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Wrong email or password",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}
