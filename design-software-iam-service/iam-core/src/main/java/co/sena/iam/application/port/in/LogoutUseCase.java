package co.sena.iam.application.port.in;

public interface LogoutUseCase {
    /** E2: revoca el refresh_token recibido. */
    void logout(String rawRefreshToken);
}
