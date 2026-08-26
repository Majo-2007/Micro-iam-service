package co.sena.iam.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModuleJpaRepository extends JpaRepository<ModuleJpaEntity, UUID> {
    List<ModuleJpaEntity> findByActiveTrueOrderByDisplayOrderAsc();
}
