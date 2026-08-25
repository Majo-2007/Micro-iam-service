package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.RefreshAccessTokenUseCase;
import co.sena.iam.application.port.out.RefreshTokenCrypto;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.TokenRevokedException;
import co.sena.iam.domain.model.RefreshToken;
import co.sena.iam.domain.model.User;

import java.time.Clock;
import java.time.Instant;

/** HU-IAM-002 E1: refresco de access_token sin rotar el refresh_token. */
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    public RefreshAccessTokenService(RefreshTokenRepository refreshTokenRepository,
                                      RefreshTokenCrypto refreshTokenCrypto,
                                      UserRepository userRepository,
                                      TokenIssuer tokenIssuer,
                                      Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenCrypto = refreshTokenCrypto;
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Override
    public AccessTokenResult refresh(String rawRefreshToken) {
        Instant now = clock.instant();
        String hash = refreshTokenCrypto.hash(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidCredentialsException::new);

        // E2: reusar un refresh ya revocado -> 401 TOKEN_REVOKED (chequeo explícito antes que expiración,
        // para dar el código de error correcto).
        if (refreshToken.isRevoked()) {
            throw new TokenRevokedException();
        }
        if (refreshToken.isExpiredAt(now)) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findById(refreshToken.userId())
                .orElseThrow(InvalidCredentialsException::new);

        TokenIssuer.IssuedToken accessToken = tokenIssuer.issueAccessToken(user);
        // Sin rotación: el mismo refresh_token sigue siendo válido hasta su expiración o revocación.
        return new AccessTokenResult(accessToken.value(), "Bearer", accessToken.expiresInSeconds());
    }
}
