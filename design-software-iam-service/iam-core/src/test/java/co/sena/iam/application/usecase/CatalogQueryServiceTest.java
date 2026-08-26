package co.sena.iam.application.usecase;

import co.sena.iam.application.port.out.ModuleRepository;
import co.sena.iam.application.port.out.RoleFeatureRepository;
import co.sena.iam.application.port.out.RoleRepository;
import co.sena.iam.domain.exception.RoleNotFoundException;
import co.sena.iam.domain.model.RbacModule;
import co.sena.iam.domain.model.RbacRole;
import co.sena.iam.domain.model.RoleFeatureGrant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogQueryServiceTest {

    private UUID coordinatorRoleId;
    private CatalogQueryService service;

    @BeforeEach
    void setUp() {
        coordinatorRoleId = UUID.randomUUID();
        RbacRole coordinator = new RbacRole(coordinatorRoleId, "COORDINATOR", "Coordinador", "Coordina fichas", false);

        ModuleRepository moduleRepository = () -> List.of(
                new RbacModule(UUID.randomUUID(), "SCHEDULES", "Horarios", null, (short) 1, "calendar"));
        RoleRepository roleRepository = new RoleRepository() {
            public List<RbacRole> findAll() { return List.of(coordinator); }
            public Optional<RbacRole> findById(UUID id) {
                return id.equals(coordinatorRoleId) ? Optional.of(coordinator) : Optional.empty();
            }
        };
        RoleFeatureRepository roleFeatureRepository = roleId -> List.of(
                new RoleFeatureGrant(UUID.randomUUID(), "SCHEDULES_WRITE", "Editar horarios", "WRITE", "TRAINING_CENTER"));

        service = new CatalogQueryService(moduleRepository, roleRepository, roleFeatureRepository);
    }

    @Test
    void e1_listaModulos() {
        assertEquals(1, service.listModules().size());
    }

    @Test
    void e1_listaRoles() {
        assertEquals(1, service.listRoles().size());
    }

    @Test
    void e3_listaCaracteristicasDeUnRolExistente() {
        var grants = service.listRoleFeatures(coordinatorRoleId);
        assertEquals(1, grants.size());
        assertEquals("TRAINING_CENTER", grants.get(0).scopeType());
    }

    @Test
    void listarCaracteristicasDeRolInexistenteLanzaRoleNotFound() {
        assertThrows(RoleNotFoundException.class, () -> service.listRoleFeatures(UUID.randomUUID()));
    }
}
