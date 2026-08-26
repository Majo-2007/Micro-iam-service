package co.sena.iam.application.port.in;

import co.sena.iam.domain.model.RoleFeatureGrant;

import java.util.List;
import java.util.UUID;

public interface ListRoleFeaturesUseCase {
    /** Lanza RoleNotFoundException si el rol no existe (404). */
    List<RoleFeatureGrant> listRoleFeatures(UUID roleId);
}
