package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.LoginUseCase;
import co.sena.iam.application.port.out.AuditLoginRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.model.AuditLoginEntry;
import co.sena.iam.domain.model.LoginOutcome;
import co.sena.iam.domain.model.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * HU-IAM-001: inicio de sesión, bloqueo por intentos y auditoría.
 * E1 login OK · E2 credenciales inválidas · E3 bloqueo por RN-IAM-01.
 * Sin Spring: se instancia e inyecta desde iam-api (config de wiring hexagonal).
 */
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final AuditLoginRepository auditLoginRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    public LoginService(UserRepository userRepository,
                         AuditLoginRepository auditLoginRepository,
                         PasswordHasher passwordHasher,
                         TokenIssuer tokenIssuer,
                         Clock clock) {
        this.userRepository = userRepository;
        this.auditLoginRepository = auditLoginRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        Instant now = clock.instant();
        Optional<User> maybeUser = userRepository.findByEmail(command.email());

        if (maybeUser.isEmpty()) {
            audit(null, command, LoginOutcome.USER_NOT_FOUND, now);
            throw new InvalidCredentialsException();
        }

        User user = maybeUser.get();

        if (user.isLockedAt(now)) {
            audit(user.id(), command, LoginOutcome.ACCOUNT_LOCKED, now);
            throw new AccountLockedException(user.lockedUntil());
        }

        if (!user.active() || !passwordHasher.matches(command.rawPassword(), user.passwordHash())) {
            user.registerFailedAttempt(now);
            userRepository.save(user);
            audit(user.id(), command, LoginOutcome.INVALID_PASSWORD, now);
            throw new InvalidCredentialsException();
        }

        user.registerSuccessfulLogin(now);
        userRepository.save(user);
        audit(user.id(), command, LoginOutcome.SUCCESS, now);

        TokenIssuer.IssuedToken accessToken = tokenIssuer.issueAccessToken(user);
        return new LoginResult(accessToken.value(), "Bearer", accessToken.expiresInSeconds());
    }

    private void audit(java.util.UUID userId, LoginCommand command, LoginOutcome outcome, Instant now) {
        auditLoginRepository.record(new AuditLoginEntry(
                userId, command.email(), outcome, command.ipAddress(), command.userAgent(), now
        ));
    }
}
