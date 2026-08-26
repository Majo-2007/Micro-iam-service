package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RoleFeatureGrant;

import java.util.List;
import java.util.UUID;

public interface RoleFeatureRepository {
    /** E3: GET /roles/{id}/features */
    List<RoleFeatureGrant> findByRoleId(UUID roleId);
}
