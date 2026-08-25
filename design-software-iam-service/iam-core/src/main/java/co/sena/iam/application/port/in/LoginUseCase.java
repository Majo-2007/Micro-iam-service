package co.sena.iam.application.port.in;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);

    record LoginCommand(String email, String rawPassword, String ipAddress, String userAgent) {}

    /** refreshToken (HU-IAM-002): TTL de 7 días, se persiste solo su hash (session.refresh_token). */
    record LoginResult(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}
}
