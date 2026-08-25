package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", schema = "session")
public class RefreshTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "device_hint")
    private String deviceHint;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RefreshTokenJpaEntity() {
        // JPA
    }

    public RefreshTokenJpaEntity(UUID id, UUID userId, String tokenHash, String deviceHint, String ipAddress,
                                  Instant expiresAt, boolean revoked, Instant revokedAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.deviceHint = deviceHint;
        this.ipAddress = ipAddress;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public String getDeviceHint() { return deviceHint; }
    public String getIpAddress() { return ipAddress; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
