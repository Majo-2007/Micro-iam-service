package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.RoleFeatureRepository;
import co.sena.iam.domain.model.RoleFeatureGrant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RoleFeatureRepositoryAdapter implements RoleFeatureRepository {

    private final RoleFeatureJpaRepository jpaRepository;

    public RoleFeatureRepositoryAdapter(RoleFeatureJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RoleFeatureGrant> findByRoleId(UUID roleId) {
        return jpaRepository.findByRoleIdWithFeature(roleId).stream()
                .map(rf -> new RoleFeatureGrant(
                        rf.getFeature().getId(), rf.getFeature().getCode(), rf.getFeature().getName(),
                        rf.getFeature().getActionLevel(), rf.getScopeType()))
                .toList();
    }
}
