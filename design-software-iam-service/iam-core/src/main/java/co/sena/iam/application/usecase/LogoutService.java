package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.LogoutUseCase;
import co.sena.iam.application.port.out.RefreshTokenCrypto;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.TokenRevokedException;
import co.sena.iam.domain.model.RefreshToken;

import java.time.Clock;

/** HU-IAM-002 E2: cierre de sesión, revoca el refresh_token. */
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final Clock clock;

    public LogoutService(RefreshTokenRepository refreshTokenRepository,
                          RefreshTokenCrypto refreshTokenCrypto,
                          Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenCrypto = refreshTokenCrypto;
        this.clock = clock;
    }

    @Override
    public void logout(String rawRefreshToken) {
        String hash = refreshTokenCrypto.hash(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidCredentialsException::new);

        // Reusar logout sobre un token ya revocado también es "reuso" -> TOKEN_REVOKED (criterio E2).
        if (refreshToken.isRevoked()) {
            throw new TokenRevokedException();
        }

        refreshToken.revoke(clock.instant());
        refreshTokenRepository.save(refreshToken);
    }
}
