package co.sena.iam.application.port.in;

import co.sena.iam.domain.model.RbacRole;

import java.util.List;

public interface ListRolesUseCase {
    List<RbacRole> listRoles();
}
