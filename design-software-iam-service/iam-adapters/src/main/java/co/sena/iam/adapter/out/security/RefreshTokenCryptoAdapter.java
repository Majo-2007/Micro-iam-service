package co.sena.iam.adapter.out.security;

import co.sena.iam.application.port.out.RefreshTokenCrypto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * HU-IAM-002: el refresh_token es un valor opaco (no JWT) de 256 bits, aleatorio.
 * Solo su hash SHA-256 se persiste en session.refresh_token.token_hash; el valor
 * crudo se entrega al cliente una única vez (en login/refresh) y no se puede recuperar.
 */
@Component
public class RefreshTokenCryptoAdapter implements RefreshTokenCrypto {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
