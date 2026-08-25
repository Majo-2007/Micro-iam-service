package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.ListUserSessionsUseCase;
import co.sena.iam.application.port.in.RevokeUserSessionUseCase;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.exception.SessionNotFoundException;
import co.sena.iam.domain.model.RefreshToken;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/** HU-IAM-002 E3: listar y revocar sesiones (multisesión / revocación de dispositivos). */
public class SessionManagementService implements ListUserSessionsUseCase, RevokeUserSessionUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public SessionManagementService(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Override
    public List<RefreshToken> listActiveSessions(UUID userId) {
        return refreshTokenRepository.findActiveByUserId(userId, clock.instant());
    }

    @Override
    public void revokeSession(UUID userId, UUID sessionId) {
        RefreshToken session = refreshTokenRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);
        // DELETE idempotente: si ya estaba revocada, no es un error.
        session.revoke(clock.instant());
        refreshTokenRepository.save(session);
    }
}
