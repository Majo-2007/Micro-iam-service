package co.sena.iam.application.port.in;

import co.sena.iam.domain.model.RbacModule;

import java.util.List;

public interface ListModulesUseCase {
    List<RbacModule> listModules();
}
