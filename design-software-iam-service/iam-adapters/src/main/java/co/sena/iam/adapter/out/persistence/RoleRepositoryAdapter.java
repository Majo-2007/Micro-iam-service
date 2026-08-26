package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.RoleRepository;
import co.sena.iam.domain.model.RbacRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RbacRole> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<RbacRole> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private RbacRole toDomain(RoleJpaEntity e) {
        return new RbacRole(e.getId(), e.getName(), e.getDisplayName(), e.getDescription(), e.isSystemRole());
    }
}
