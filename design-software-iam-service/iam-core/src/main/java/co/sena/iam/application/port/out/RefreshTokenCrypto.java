package co.sena.iam.application.port.out;

public interface RefreshTokenCrypto {
    /** Genera el valor crudo que se le entrega al cliente (nunca se persiste tal cual). */
    String generateRawToken();
    /** Hash irreversible que sí se persiste en session.refresh_token.token_hash. */
    String hash(String rawToken);
}
