package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.ListModulesUseCase;
import co.sena.iam.application.port.in.ListRoleFeaturesUseCase;
import co.sena.iam.application.port.in.ListRolesUseCase;
import co.sena.iam.application.port.out.ModuleRepository;
import co.sena.iam.application.port.out.RoleFeatureRepository;
import co.sena.iam.application.port.out.RoleRepository;
import co.sena.iam.domain.exception.RoleNotFoundException;
import co.sena.iam.domain.model.RbacModule;
import co.sena.iam.domain.model.RbacRole;
import co.sena.iam.domain.model.RoleFeatureGrant;

import java.util.List;
import java.util.UUID;

/** HU-IAM-004: catálogo RBAC de solo lectura (E1 módulos/roles, E3 rol→características). */
public class CatalogQueryService implements ListModulesUseCase, ListRolesUseCase, ListRoleFeaturesUseCase {

    private final ModuleRepository moduleRepository;
    private final RoleRepository roleRepository;
    private final RoleFeatureRepository roleFeatureRepository;

    public CatalogQueryService(ModuleRepository moduleRepository,
                                RoleRepository roleRepository,
                                RoleFeatureRepository roleFeatureRepository) {
        this.moduleRepository = moduleRepository;
        this.roleRepository = roleRepository;
        this.roleFeatureRepository = roleFeatureRepository;
    }

    @Override
    public List<RbacModule> listModules() {
        return moduleRepository.findAllActive();
    }

    @Override
    public List<RbacRole> listRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<RoleFeatureGrant> listRoleFeatures(UUID roleId) {
        roleRepository.findById(roleId).orElseThrow(RoleNotFoundException::new);
        return roleFeatureRepository.findByRoleId(roleId);
    }
}
