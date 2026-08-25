package co.sena.iam.adapter.in.rest.dto;

public record RefreshResponse(String accessToken, String tokenType, long expiresIn) {}
