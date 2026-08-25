package co.sena.iam.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLoginJpaRepository extends JpaRepository<AuditLoginJpaEntity, UUID> {
}
