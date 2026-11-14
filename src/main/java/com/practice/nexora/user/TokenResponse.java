package com.practice.nexora.user;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
