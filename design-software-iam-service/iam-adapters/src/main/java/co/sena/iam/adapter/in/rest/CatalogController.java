package co.sena.iam.adapter.in.rest;

import co.sena.iam.adapter.in.rest.dto.ModuleResponse;
import co.sena.iam.adapter.in.rest.dto.RoleFeatureResponse;
import co.sena.iam.adapter.in.rest.dto.RoleResponse;
import co.sena.iam.application.port.in.ListModulesUseCase;
import co.sena.iam.application.port.in.ListRoleFeaturesUseCase;
import co.sena.iam.application.port.in.ListRolesUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * HU-IAM-004: catálogo RBAC de solo lectura.
 * Requiere estar autenticado (Bearer), pero AÚN NO restringe por rol (ej. solo ADMIN):
 * esa autorización granular por característica+alcance es HU-IAM-006, todavía no implementada.
 */
@RestController
public class CatalogController {

    private final ListModulesUseCase listModulesUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final ListRoleFeaturesUseCase listRoleFeaturesUseCase;
    private final TokenVerifier tokenVerifier;

    public CatalogController(ListModulesUseCase listModulesUseCase,
                              ListRolesUseCase listRolesUseCase,
                              ListRoleFeaturesUseCase listRoleFeaturesUseCase,
                              TokenVerifier tokenVerifier) {
        this.listModulesUseCase = listModulesUseCase;
        this.listRolesUseCase = listRolesUseCase;
        this.listRoleFeaturesUseCase = listRoleFeaturesUseCase;
        this.tokenVerifier = tokenVerifier;
    }

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleResponse>> modules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireAuthenticated(authHeader);
        return ResponseEntity.ok(listModulesUseCase.listModules().stream().map(ModuleResponse::from).toList());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> roles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireAuthenticated(authHeader);
        return ResponseEntity.ok(listRolesUseCase.listRoles().stream().map(RoleResponse::from).toList());
    }

    @GetMapping("/roles/{roleId}/features")
    public ResponseEntity<List<RoleFeatureResponse>> roleFeatures(
            @PathVariable UUID roleId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireAuthenticated(authHeader);
        var grants = listRoleFeaturesUseCase.listRoleFeatures(roleId).stream()
                .map(RoleFeatureResponse::from)
                .toList();
        return ResponseEntity.ok(grants);
    }

    private void requireAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        tokenVerifier.verifyAndGetUserId(authHeader.substring("Bearer ".length()));
    }
}
