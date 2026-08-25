package co.sena.iam.application.usecase;

import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.exception.SessionNotFoundException;
import co.sena.iam.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagementServiceTest {

    private final Instant now = Instant.parse("2026-08-24T12:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    private List<RefreshToken> sessions;
    private SessionManagementService service;

    @BeforeEach
    void setUp() {
        sessions = new ArrayList<>();
        RefreshTokenRepository repo = new RefreshTokenRepository() {
            public void save(RefreshToken t) {
                sessions.removeIf(s -> s.id().equals(t.id()));
                sessions.add(t);
            }
            public Optional<RefreshToken> findByTokenHash(String h) { throw new UnsupportedOperationException(); }
            public Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId) {
                return sessions.stream().filter(s -> s.id().equals(id) && s.userId().equals(userId)).findFirst();
            }
            public List<RefreshToken> findActiveByUserId(UUID userId, Instant now) {
                return sessions.stream()
                        .filter(s -> s.userId().equals(userId) && s.isActiveAt(now))
                        .toList();
            }
        };
        service = new SessionManagementService(repo, clock);
    }

    @Test
    void e3_listaSesionesActivasDelUsuario() {
        UUID userId = UUID.randomUUID();
        sessions.add(RefreshToken.issue(userId, "h1", "chrome", "127.0.0.1", now));
        sessions.add(RefreshToken.issue(userId, "h2", "firefox", "127.0.0.1", now));
        sessions.add(RefreshToken.issue(UUID.randomUUID(), "h3", "otro-usuario", "127.0.0.1", now)); // otro user

        var result = service.listActiveSessions(userId);

        assertEquals(2, result.size());
    }

    @Test
    void e3_revocarSesionLaMarcaComoRevocada() {
        UUID userId = UUID.randomUUID();
        RefreshToken session = RefreshToken.issue(userId, "h1", "chrome", "127.0.0.1", now);
        sessions.add(session);

        service.revokeSession(userId, session.id());

        assertTrue(sessions.get(0).isRevoked());
        assertTrue(service.listActiveSessions(userId).isEmpty());
    }

    @Test
    void e3_revocarSesionDeOtroUsuarioLanzaSessionNotFound() {
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();
        RefreshToken session = RefreshToken.issue(owner, "h1", "chrome", "127.0.0.1", now);
        sessions.add(session);

        assertThrows(SessionNotFoundException.class, () -> service.revokeSession(attacker, session.id()));
    }
}
