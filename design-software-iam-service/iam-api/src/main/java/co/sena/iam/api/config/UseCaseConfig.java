package co.sena.iam.api.config;

import co.sena.iam.application.port.out.AuditLoginRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.application.usecase.GetCurrentUserService;
import co.sena.iam.application.usecase.LoginService;
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

    @Bean
    public LoginService loginService(UserRepository userRepository,
                                      AuditLoginRepository auditLoginRepository,
                                      PasswordHasher passwordHasher,
                                      TokenIssuer tokenIssuer,
                                      Clock clock) {
        return new LoginService(userRepository, auditLoginRepository, passwordHasher, tokenIssuer, clock);
    }

    @Bean
    public GetCurrentUserService getCurrentUserService(UserRepository userRepository) {
        return new GetCurrentUserService(userRepository);
    }
}
