package com.practice.nexora.user;

public record UserResponse(String email, Role role) {

    static UserResponse from(UserAccount account) {
        return new UserResponse(account.getEmail(), account.getRole());
    }
}
