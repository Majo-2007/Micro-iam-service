package co.sena.iam.application.usecase;

import co.sena.iam.application.port.out.RefreshTokenCrypto;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.TokenRevokedException;
import co.sena.iam.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LogoutServiceTest {

    private final Instant now = Instant.parse("2026-08-24T12:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private final RefreshTokenCrypto crypto = new RefreshTokenCrypto() {
        public String generateRawToken() { return UUID.randomUUID().toString(); }
        public String hash(String rawToken) { return "hash-of-" + rawToken; }
    };

    private Map<String, RefreshToken> byHash;
    private LogoutService service;

    @BeforeEach
    void setUp() {
        byHash = new HashMap<>();
        RefreshTokenRepository repo = new RefreshTokenRepository() {
            public void save(RefreshToken t) { byHash.put(t.tokenHash(), t); }
            public Optional<RefreshToken> findByTokenHash(String h) { return Optional.ofNullable(byHash.get(h)); }
            public Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId) { throw new UnsupportedOperationException(); }
            public List<RefreshToken> findActiveByUserId(UUID userId, Instant now) { throw new UnsupportedOperationException(); }
        };
        service = new LogoutService(repo, crypto, clock);
    }

    @Test
    void e2_logoutRevocaElRefreshToken() {
        String raw = "raw-1";
        RefreshToken token = RefreshToken.issue(UUID.randomUUID(), crypto.hash(raw), "chrome", "127.0.0.1", now);
        byHash.put(crypto.hash(raw), token);

        service.logout(raw);

        assertTrue(byHash.get(crypto.hash(raw)).isRevoked());
    }

    @Test
    void e2_reusarLogoutSobreTokenYaRevocadoLanzaTokenRevoked() {
        String raw = "raw-2";
        RefreshToken token = RefreshToken.issue(UUID.randomUUID(), crypto.hash(raw), "chrome", "127.0.0.1", now);
        token.revoke(now);
        byHash.put(crypto.hash(raw), token);

        assertThrows(TokenRevokedException.class, () -> service.logout(raw));
    }

    @Test
    void logoutConTokenInexistenteLanzaInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> service.logout("no-existe"));
    }
}
