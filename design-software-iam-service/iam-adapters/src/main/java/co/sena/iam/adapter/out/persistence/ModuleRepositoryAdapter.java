package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.ModuleRepository;
import co.sena.iam.domain.model.RbacModule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModuleRepositoryAdapter implements ModuleRepository {

    private final ModuleJpaRepository jpaRepository;

    public ModuleRepositoryAdapter(ModuleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RbacModule> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(e -> new RbacModule(e.getId(), e.getCode(), e.getName(), e.getDescription(),
                        e.getDisplayOrder(), e.getIconKey()))
                .toList();
    }
}
