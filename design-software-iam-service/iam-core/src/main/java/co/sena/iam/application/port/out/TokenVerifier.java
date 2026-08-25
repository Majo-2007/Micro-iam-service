package co.sena.iam.application.port.out;

import java.util.UUID;

public interface TokenVerifier {
    /** @return el userId (sub) si el token es válido; lanza si es inválido/expirado. */
    UUID verifyAndGetUserId(String token);
}
