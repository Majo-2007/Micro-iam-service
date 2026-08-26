package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RbacRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    /** E1: GET /roles */
    List<RbacRole> findAll();
    Optional<RbacRole> findById(UUID id);
}
