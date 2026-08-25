package co.sena.iam.adapter.out.security;

import co.sena.iam.application.port.out.TokenIssuer;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * HU-IAM-010: emisión y verificación local del JWT (claims: sub, email, actorType, exp).
 * El resto de servicios podrán verificar localmente con la misma clave/JWKS (pendiente definir HU-IAM-010 E3).
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenService implements TokenIssuer, TokenVerifier {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IssuedToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenTtlSeconds());

        String token = Jwts.builder()
                .subject(user.id().toString())
                .issuer(properties.getIssuer())
                .claim("email", user.email())
                .claim("actorType", user.actorType())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();

        return new IssuedToken(token, properties.getAccessTokenTtlSeconds());
    }

    @Override
    public UUID verifyAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException();
        }
    }
}
