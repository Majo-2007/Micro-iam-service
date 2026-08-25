package co.sena.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/** identity_audit.audit_login: registro inmutable de cada intento de login (HU-IAM-001, HU-IAM-009). */
public record AuditLoginEntry(
        UUID userId,
        String emailAttempted,
        LoginOutcome outcome,
        String ipAddress,
        String userAgent,
        Instant attemptedAt
) {}
