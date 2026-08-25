package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.LoginUseCase.LoginCommand;
import co.sena.iam.application.port.out.AuditLoginRepository;
import co.sena.iam.application.port.out.PasswordHasher;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.model.AuditLoginEntry;
import co.sena.iam.domain.model.LoginOutcome;
import co.sena.iam.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    private final Instant fixedNow = Instant.parse("2026-08-24T12:00:00Z");
    private final Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

    private Map<String, User> usersByEmail;
    private List<AuditLoginEntry> auditTrail;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        usersByEmail = new HashMap<>();
        auditTrail = new ArrayList<>();

        UserRepository userRepository = new UserRepository() {
            public Optional<User> findByEmail(String email) { return Optional.ofNullable(usersByEmail.get(email)); }
            public Optional<User> findById(UUID id) {
                return usersByEmail.values().stream().filter(u -> u.id().equals(id)).findFirst();
            }
            public void save(User user) { usersByEmail.put(user.email(), user); }
        };
        AuditLoginRepository auditLoginRepository = auditTrail::add;
        PasswordHasher passwordHasher = (raw, hash) -> raw.equals(hash);
        TokenIssuer tokenIssuer = user -> new TokenIssuer.IssuedToken("fake-jwt-" + user.id(), 900);

        loginService = new LoginService(userRepository, auditLoginRepository, passwordHasher, tokenIssuer, clock);
    }

    private User newUser() {
        return new User(UUID.randomUUID(), "ana@sena.co", "secret", "Ana", "Perez",
                "USER", null, true, null, (short) 0, null);
    }

    @Test
    void e1_loginOkEmiteTokenYReiniciaContador() {
        User user = newUser();
        usersByEmail.put(user.email(), user);

        var result = loginService.login(new LoginCommand(user.email(), "secret", "127.0.0.1", "junit"));

        assertNotNull(result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(0, user.failedAttempts());
        assertEquals(LoginOutcome.SUCCESS, auditTrail.get(0).outcome());
    }

    @Test
    void e2_passwordInvalidaIncrementaContadorYAuditaFallo() {
        User user = newUser();
        usersByEmail.put(user.email(), user);

        assertThrows(InvalidCredentialsException.class, () ->
                loginService.login(new LoginCommand(user.email(), "wrong", "127.0.0.1", "junit")));

        assertEquals(1, user.failedAttempts());
        assertEquals(LoginOutcome.INVALID_PASSWORD, auditTrail.get(0).outcome());
    }

    @Test
    void e2_usuarioInexistenteEsInvalidCredentialsYAuditaUserNotFound() {
        assertThrows(InvalidCredentialsException.class, () ->
                loginService.login(new LoginCommand("nadie@sena.co", "x", "127.0.0.1", "junit")));

        assertEquals(LoginOutcome.USER_NOT_FOUND, auditTrail.get(0).outcome());
    }

    @Test
    void e3_quintoFalloBloqueaCuenta15Minutos() {
        User user = newUser();
        usersByEmail.put(user.email(), user);

        for (int i = 0; i < 5; i++) {
            assertThrows(InvalidCredentialsException.class, () ->
                    loginService.login(new LoginCommand(user.email(), "wrong", "127.0.0.1", "junit")));
        }

        assertTrue(user.isLockedAt(fixedNow));
        assertEquals(fixedNow.plusSeconds(15 * 60), user.lockedUntil());
    }

    // La escalada a 24h (10º fallo) es una regla de User, no de la orquestación de LoginService:
    // una vez bloqueada la cuenta al 5º fallo, login() ya no vuelve a intentar password
    // (lanza ACCOUNT_LOCKED antes), así que no se puede "seguir fallando" vía el flujo HTTP.
    // Se prueba directo sobre el dominio en UserTest#decimoFalloEscalaBloqueoA24Horas.

    @Test
    void e3_iniciarSesionEstandoBloqueadoLanzaAccountLocked() {
        User user = newUser();
        for (int i = 0; i < 5; i++) user.registerFailedAttempt(fixedNow);
        usersByEmail.put(user.email(), user);

        assertThrows(AccountLockedException.class, () ->
                loginService.login(new LoginCommand(user.email(), "secret", "127.0.0.1", "junit")));

        assertEquals(LoginOutcome.ACCOUNT_LOCKED, auditTrail.get(0).outcome());
    }
}
