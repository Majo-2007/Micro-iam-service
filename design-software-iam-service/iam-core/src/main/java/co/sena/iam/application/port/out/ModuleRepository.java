package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.RbacModule;

import java.util.List;

public interface ModuleRepository {
    /** E1: GET /modules, ordenado por display_order, solo módulos activos. */
    List<RbacModule> findAllActive();
}
