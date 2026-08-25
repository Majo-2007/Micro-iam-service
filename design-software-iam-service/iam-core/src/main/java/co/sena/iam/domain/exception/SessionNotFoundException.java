package co.sena.iam.domain.exception;

/** E3: DELETE /users/{id}/sessions/{sid} sobre una sesión que no existe o no es de ese usuario -> 404. */
public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException() {
        super("SESSION_NOT_FOUND");
    }
}
