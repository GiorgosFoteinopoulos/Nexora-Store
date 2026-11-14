package com.practice.nexora.user;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountAdminService {

    private final UserAccountRepository accounts;

    public AccountAdminService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listAccounts() {
        return accounts.findAll(Sort.by("email")).stream()
                .map(UserResponse::from)
                .toList();
    }
}
