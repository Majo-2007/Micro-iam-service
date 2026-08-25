package co.sena.iam.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);
    Optional<RefreshTokenJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<RefreshTokenJpaEntity> findByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, Instant now);
}
