package co.sena.iam.domain.exception;

/** E2: reusar un refresh token ya revocado -> 401 TOKEN_REVOKED. */
public class TokenRevokedException extends RuntimeException {
    public TokenRevokedException() {
        super("TOKEN_REVOKED");
    }
}
