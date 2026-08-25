package co.sena.iam.application.port.in;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);

    record LoginCommand(String email, String rawPassword, String ipAddress, String userAgent) {}

    record LoginResult(String accessToken, String tokenType, long expiresInSeconds) {}
}
