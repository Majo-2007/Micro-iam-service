package co.sena.iam.domain.exception;

import java.time.Instant;

/** E3: cuenta bloqueada por RN-IAM-01 -> 423/401 ACCOUNT_LOCKED. */
public class AccountLockedException extends RuntimeException {
    private final Instant lockedUntil;

    public AccountLockedException(Instant lockedUntil) {
        super("ACCOUNT_LOCKED");
        this.lockedUntil = lockedUntil;
    }

    public Instant lockedUntil() {
        return lockedUntil;
    }
}
