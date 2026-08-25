package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = jpaRepository.findById(refreshToken.id())
                .orElseGet(() -> new RefreshTokenJpaEntity(
                        refreshToken.id(), refreshToken.userId(), refreshToken.tokenHash(),
                        refreshToken.deviceHint(), refreshToken.ipAddress(), refreshToken.expiresAt(),
                        refreshToken.isRevoked(), refreshToken.revokedAt(), refreshToken.createdAt()));
        entity.setRevoked(refreshToken.isRevoked());
        entity.setRevokedAt(refreshToken.revokedAt());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId, Instant now) {
        return jpaRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(userId, now)
                .stream().map(this::toDomain).toList();
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity e) {
        return new RefreshToken(e.getId(), e.getUserId(), e.getTokenHash(), e.getDeviceHint(), e.getIpAddress(),
                e.getCreatedAt(), e.getExpiresAt(), e.isRevoked(), e.getRevokedAt());
    }
}
