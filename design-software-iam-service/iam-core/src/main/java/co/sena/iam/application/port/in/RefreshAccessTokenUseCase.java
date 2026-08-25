package co.sena.iam.application.port.in;

public interface RefreshAccessTokenUseCase {
    /** E1: nuevo access_token a partir de un refresh_token válido. No rota el refresh. */
    AccessTokenResult refresh(String rawRefreshToken);

    record AccessTokenResult(String accessToken, String tokenType, long expiresInSeconds) {}
}
