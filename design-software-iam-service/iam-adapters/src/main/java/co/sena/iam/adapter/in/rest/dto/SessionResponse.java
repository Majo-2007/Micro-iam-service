package co.sena.iam.adapter.in.rest.dto;

import co.sena.iam.domain.model.RefreshToken;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(UUID id, String deviceHint, String ipAddress, Instant createdAt, Instant expiresAt) {
    public static SessionResponse from(RefreshToken refreshToken) {
        return new SessionResponse(refreshToken.id(), refreshToken.deviceHint(), refreshToken.ipAddress(),
                refreshToken.createdAt(), refreshToken.expiresAt());
    }
}
