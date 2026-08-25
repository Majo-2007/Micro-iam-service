package co.sena.iam.adapter.in.rest.dto;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {}
