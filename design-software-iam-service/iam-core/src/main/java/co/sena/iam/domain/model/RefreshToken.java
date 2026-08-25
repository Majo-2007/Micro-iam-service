package co.sena.iam.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Agregado de dominio: session.refresh_token (HU-IAM-002).
 * Nunca guarda el token en claro: solo su hash (ver RefreshTokenCrypto). El valor crudo
 * se devuelve al cliente una sola vez, en el login.
 */
public class RefreshToken {

    /** RF-IAM-01: sesión de 7 días, fija por regla de negocio (no configurable por ambiente). */
    public static final Duration TTL = Duration.ofDays(7);

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final String deviceHint;
    private final String ipAddress;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean revoked;
    private Instant revokedAt;

    public RefreshToken(UUID id, UUID userId, String tokenHash, String deviceHint, String ipAddress,
                         Instant createdAt, Instant expiresAt, boolean revoked, Instant revokedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.deviceHint = deviceHint;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
    }

    /** Emite una sesión nueva al hacer login (HU-IAM-001/002), TTL fijo de 7 días. */
    public static RefreshToken issue(UUID userId, String tokenHash, String deviceHint, String ipAddress, Instant now) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, deviceHint, ipAddress,
                now, now.plus(TTL), false, null);
    }

    public boolean isExpiredAt(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isActiveAt(Instant now) {
        return !revoked && !isExpiredAt(now);
    }

    /** E2/E3: revocar en logout o al eliminar una sesión puntual. Idempotente. */
    public void revoke(Instant now) {
        if (this.revoked) return;
        this.revoked = true;
        this.revokedAt = now;
    }

    public UUID id() { return id; }
    public UUID userId() { return userId; }
    public String tokenHash() { return tokenHash; }
    public String deviceHint() { return deviceHint; }
    public String ipAddress() { return ipAddress; }
    public Instant createdAt() { return createdAt; }
    public Instant expiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Instant revokedAt() { return revokedAt; }
}
