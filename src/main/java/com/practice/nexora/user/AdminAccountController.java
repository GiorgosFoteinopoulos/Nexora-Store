package com.practice.nexora.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "Admins only")
@RestController
@RequestMapping("/api/admin")
class AdminAccountController {

    private final AccountAdminService accountAdminService;

    AdminAccountController(AccountAdminService accountAdminService) {
        this.accountAdminService = accountAdminService;
    }

    @Operation(summary = "List all accounts", security = @SecurityRequirement(name = "bearer"))
    @GetMapping("/accounts")
    List<UserResponse> listAccounts() {
        return accountAdminService.listAccounts();
    }
}
