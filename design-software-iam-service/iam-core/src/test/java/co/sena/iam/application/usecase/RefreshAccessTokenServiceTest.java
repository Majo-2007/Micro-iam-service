package co.sena.iam.application.usecase;

import co.sena.iam.application.port.out.RefreshTokenCrypto;
import co.sena.iam.application.port.out.RefreshTokenRepository;
import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.TokenRevokedException;
import co.sena.iam.domain.model.RefreshToken;
import co.sena.iam.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshAccessTokenServiceTest {

    private final Instant now = Instant.parse("2026-08-24T12:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private final RefreshTokenCrypto crypto = new FakeCrypto();

    private Map<String, RefreshToken> byHash;
    private Map<UUID, User> usersById;
    private RefreshAccessTokenService service;

    @BeforeEach
    void setUp() {
        byHash = new HashMap<>();
        usersById = new HashMap<>();

        RefreshTokenRepository repo = new RefreshTokenRepository() {
            public void save(RefreshToken t) { byHash.put(t.tokenHash(), t); }
            public Optional<RefreshToken> findByTokenHash(String h) { return Optional.ofNullable(byHash.get(h)); }
            public Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId) { throw new UnsupportedOperationException(); }
            public java.util.List<RefreshToken> findActiveByUserId(UUID userId, Instant now) { throw new UnsupportedOperationException(); }
        };
        UserRepository userRepository = new UserRepository() {
            public Optional<User> findByEmail(String email) { throw new UnsupportedOperationException(); }
            public Optional<User> findById(UUID id) { return Optional.ofNullable(usersById.get(id)); }
            public void save(User user) { }
        };
        TokenIssuer tokenIssuer = user -> new TokenIssuer.IssuedToken("new-access-" + user.id(), 900);

        service = new RefreshAccessTokenService(repo, crypto, userRepository, tokenIssuer, clock);
    }

    private User newUser(UUID id) {
        return new User(id, "ana@sena.co", "hash", "Ana", "Perez", "USER", null, true, null, (short) 0, null);
    }

    @Test
    void e1_refreshValidoEmiteNuevoAccessTokenSinRotarElRefresh() {
        UUID userId = UUID.randomUUID();
        usersById.put(userId, newUser(userId));
        String raw = "raw-token-1";
        byHash.put(crypto.hash(raw), RefreshToken.issue(userId, crypto.hash(raw), "chrome", "127.0.0.1", now));

        var result = service.refresh(raw);

        assertEquals("new-access-" + userId, result.accessToken());
        assertTrue(byHash.containsKey(crypto.hash(raw))); // el refresh sigue igual, no se rota
    }

    @Test
    void e2_refreshRevocadoLanzaTokenRevoked() {
        UUID userId = UUID.randomUUID();
        usersById.put(userId, newUser(userId));
        String raw = "raw-token-2";
        RefreshToken token = RefreshToken.issue(userId, crypto.hash(raw), "chrome", "127.0.0.1", now);
        token.revoke(now);
        byHash.put(crypto.hash(raw), token);

        assertThrows(TokenRevokedException.class, () -> service.refresh(raw));
    }

    @Test
    void refreshExpiradoLanzaInvalidCredentials() {
        UUID userId = UUID.randomUUID();
        usersById.put(userId, newUser(userId));
        String raw = "raw-token-3";
        RefreshToken expired = new RefreshToken(UUID.randomUUID(), userId, crypto.hash(raw), null, null,
                now.minusSeconds(8 * 24 * 3600), now.minusSeconds(3600), false, null);
        byHash.put(crypto.hash(raw), expired);

        assertThrows(InvalidCredentialsException.class, () -> service.refresh(raw));
    }

    @Test
    void refreshInexistenteLanzaInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> service.refresh("no-existe"));
    }

    private static class FakeCrypto implements RefreshTokenCrypto {
        public String generateRawToken() { return UUID.randomUUID().toString(); }
        public String hash(String rawToken) { return "hash-of-" + rawToken; }
    }
}
