package co.sena.iam.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** RN-IAM-01: escalada de bloqueo por intentos fallidos, probada directo sobre el dominio. */
class UserTest {

    private final Instant now = Instant.parse("2026-08-24T12:00:00Z");

    private User newUser() {
        return new User(UUID.randomUUID(), "ana@sena.co", "secret", "Ana", "Perez",
                "USER", null, true, null, (short) 0, null);
    }

    @Test
    void quintoFalloBloqueaCuenta15Minutos() {
        User user = newUser();
        for (int i = 0; i < 5; i++) user.registerFailedAttempt(now);

        assertTrue(user.isLockedAt(now));
        assertEquals(now.plus(Duration.ofMinutes(15)), user.lockedUntil());
    }

    @Test
    void decimoFalloEscalaBloqueoA24Horas() {
        User user = newUser();
        for (int i = 0; i < 10; i++) user.registerFailedAttempt(now);

        assertEquals(now.plus(Duration.ofHours(24)), user.lockedUntil());
    }

    @Test
    void loginExitosoReiniciaContadorYDesbloquea() {
        User user = newUser();
        for (int i = 0; i < 5; i++) user.registerFailedAttempt(now);
        assertTrue(user.isLockedAt(now));

        user.registerSuccessfulLogin(now.plusSeconds(1));

        assertEquals(0, user.failedAttempts());
        assertFalse(user.isLockedAt(now.plusSeconds(1)));
        assertEquals(now.plusSeconds(1), user.lastLoginAt());
    }
}
