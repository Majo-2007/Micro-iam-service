package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.AuditLoginEntry;

public interface AuditLoginRepository {
    void record(AuditLoginEntry entry);
}
