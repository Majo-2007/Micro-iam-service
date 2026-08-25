package co.sena.iam.application.port.out;

public interface PasswordHasher {
    boolean matches(String rawPassword, String passwordHash);
}
