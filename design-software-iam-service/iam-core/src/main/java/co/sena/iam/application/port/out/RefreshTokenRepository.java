package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId);
    /** E3: sesiones activas (no revocadas, no expiradas) de un usuario. */
    List<RefreshToken> findActiveByUserId(UUID userId, Instant now);
}
