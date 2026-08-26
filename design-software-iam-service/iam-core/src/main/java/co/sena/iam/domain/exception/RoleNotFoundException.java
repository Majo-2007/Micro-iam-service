package co.sena.iam.domain.exception;

/** GET /roles/{id}/features sobre un rol que no existe -> 404. */
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException() {
        super("ROLE_NOT_FOUND");
    }
}
