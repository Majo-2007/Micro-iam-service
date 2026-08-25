package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_login", schema = "identity_audit")
public class AuditLoginJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email_attempted", nullable = false)
    private String emailAttempted;

    @Column(nullable = false)
    private String outcome;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    protected AuditLoginJpaEntity() {
        // JPA
    }

    public AuditLoginJpaEntity(UUID userId, String emailAttempted, String outcome,
                                String ipAddress, String userAgent, Instant attemptedAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.emailAttempted = emailAttempted;
        this.outcome = outcome;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.attemptedAt = attemptedAt;
    }
}
