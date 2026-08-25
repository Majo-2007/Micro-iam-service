package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.AuditLoginRepository;
import co.sena.iam.domain.model.AuditLoginEntry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditLoginRepositoryAdapter implements AuditLoginRepository {

    private final AuditLoginJpaRepository jpaRepository;

    public AuditLoginRepositoryAdapter(AuditLoginJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    // REQUIRES_NEW: la auditoría de un intento fallido debe quedar registrada
    // incluso si el flujo de login lanza una excepción y hace rollback.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditLoginEntry entry) {
        jpaRepository.save(new AuditLoginJpaEntity(
                entry.userId(), entry.emailAttempted(), entry.outcome().name(),
                entry.ipAddress(), entry.userAgent(), entry.attemptedAt()
        ));
    }
}
