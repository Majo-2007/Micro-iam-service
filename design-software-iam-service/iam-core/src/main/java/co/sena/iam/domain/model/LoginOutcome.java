package co.sena.iam.domain.model;

/** Espejo del CHECK identity_audit.audit_login.outcome. */
public enum LoginOutcome {
    SUCCESS,
    INVALID_PASSWORD,
    USER_NOT_FOUND,
    ACCOUNT_LOCKED,
    TOKEN_EXPIRED
}
