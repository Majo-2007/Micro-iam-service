package co.sena.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Agregado de dominio: identity.user (ver design-software-iam-db).
 * Mutable a propósito: el propio agregado aplica sus invariantes de bloqueo (RN-IAM-01).
 */
public class User {

    public static final int LOCK_THRESHOLD_SHORT = 5;
    public static final int LOCK_THRESHOLD_LONG = 10;
    public static final long LOCK_MINUTES_SHORT = 15;
    public static final long LOCK_HOURS_LONG = 24;

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String actorType;
    private final UUID actorId;
    private final boolean active;
    private Instant lastLoginAt;
    private short failedAttempts;
    private Instant lockedUntil;

    public User(UUID id, String email, String passwordHash, String firstName, String lastName,
                String actorType, UUID actorId, boolean active, Instant lastLoginAt,
                short failedAttempts, Instant lockedUntil) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.actorType = actorType;
        this.actorId = actorId;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }

    /** E3: ¿la cuenta está bloqueada en este instante? */
    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /** E2: registra un fallo de autenticación y aplica RN-IAM-01. */
    public void registerFailedAttempt(Instant now) {
        this.failedAttempts = (short) (this.failedAttempts + 1);
        if (this.failedAttempts >= LOCK_THRESHOLD_LONG) {
            this.lockedUntil = now.plus(java.time.Duration.ofHours(LOCK_HOURS_LONG));
        } else if (this.failedAttempts >= LOCK_THRESHOLD_SHORT) {
            this.lockedUntil = now.plus(java.time.Duration.ofMinutes(LOCK_MINUTES_SHORT));
        }
    }

    /** E1: login exitoso reinicia el contador de fallos. */
    public void registerSuccessfulLogin(Instant now) {
        this.failedAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = now;
    }

    public UUID id() { return id; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String actorType() { return actorType; }
    public UUID actorId() { return actorId; }
    public boolean active() { return active; }
    public Instant lastLoginAt() { return lastLoginAt; }
    public short failedAttempts() { return failedAttempts; }
    public Instant lockedUntil() { return lockedUntil; }
}
