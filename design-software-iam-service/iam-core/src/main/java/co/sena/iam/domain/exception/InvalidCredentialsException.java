package co.sena.iam.domain.exception;

/** E2: credenciales inválidas (usuario inexistente o password incorrecta) -> 401 INVALID_CREDENTIALS. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS");
    }
}
