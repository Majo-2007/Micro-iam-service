package co.sena.iam.api.config;

import co.sena.iam.application.port.out.AuditLoginRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.RefreshTokenCrypto;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.application.usecase.GetCurrentUserService;
import co.sena.iam.application.usecase.CatalogQueryService;
import co.sena.iam.application.usecase.LoginService;
import co.sena.iam.application.usecase.LogoutService;
import co.sena.iam.application.usecase.RefreshAccessTokenService;
import co.sena.iam.application.usecase.SessionManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wiring hexagonal: iam-core (dominio puro, sin Spring) recibe aquí sus adaptadores
 * como implementaciones concretas de los puertos "out". Este es el único lugar
 * donde iam-api "sabe" cómo se arma un caso de uso.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // HU-IAM-001 (+ E1 de HU-IAM-002: el login ya emite y persiste el refresh_token)
    @Bean
    public LoginService loginService(UserRepository userRepository,
                                      AuditLoginRepository auditLoginRepository,
                                      PasswordHasher passwordHasher,
                                      TokenIssuer tokenIssuer,
                                      RefreshTokenRepository refreshTokenRepository,
                                      RefreshTokenCrypto refreshTokenCrypto,
                                      Clock clock) {
        return new LoginService(userRepository, auditLoginRepository, passwordHasher, tokenIssuer,
                refreshTokenRepository, refreshTokenCrypto, clock);
    }

    @Bean
    public GetCurrentUserService getCurrentUserService(UserRepository userRepository) {
        return new GetCurrentUserService(userRepository);
    }

    // HU-IAM-002 E1: POST /auth/refresh
    @Bean
    public RefreshAccessTokenService refreshAccessTokenService(RefreshTokenRepository refreshTokenRepository,
                                                                 RefreshTokenCrypto refreshTokenCrypto,
                                                                 UserRepository userRepository,
                                                                 TokenIssuer tokenIssuer,
                                                                 Clock clock) {
        return new RefreshAccessTokenService(refreshTokenRepository, refreshTokenCrypto, userRepository, tokenIssuer, clock);
    }

    // HU-IAM-002 E2: POST /auth/logout
    @Bean
    public LogoutService logoutService(RefreshTokenRepository refreshTokenRepository,
                                        RefreshTokenCrypto refreshTokenCrypto,
                                        Clock clock) {
        return new LogoutService(refreshTokenRepository, refreshTokenCrypto, clock);
    }

    // HU-IAM-002 E3: GET/DELETE /users/{id}/sessions
    @Bean
    public SessionManagementService sessionManagementService(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        return new SessionManagementService(refreshTokenRepository, clock);
    }

    // HU-IAM-004: GET /modules, GET /roles, GET /roles/{id}/features
    @Bean
    public CatalogQueryService catalogQueryService(co.sena.iam.application.port.out.ModuleRepository moduleRepository,
                                                     co.sena.iam.application.port.out.RoleRepository roleRepository,
                                                     co.sena.iam.application.port.out.RoleFeatureRepository roleFeatureRepository) {
        return new CatalogQueryService(moduleRepository, roleRepository, roleFeatureRepository);
    }
}
