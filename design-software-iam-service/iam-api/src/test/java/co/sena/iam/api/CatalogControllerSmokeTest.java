package co.sena.iam.api;

import co.sena.iam.adapter.in.rest.CatalogController;
import co.sena.iam.adapter.in.rest.GlobalExceptionHandler;
import co.sena.iam.application.port.in.ListModulesUseCase;
import co.sena.iam.application.port.in.ListRoleFeaturesUseCase;
import co.sena.iam.application.port.in.ListRolesUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.RoleNotFoundException;
import co.sena.iam.domain.model.RbacModule;
import co.sena.iam.domain.model.RbacRole;
import co.sena.iam.domain.model.RoleFeatureGrant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU-IAM-004: GET /modules, GET /roles, GET /roles/{id}/features. */
@WebMvcTest(controllers = CatalogController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class CatalogControllerSmokeTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ListModulesUseCase listModulesUseCase;
    @MockBean
    ListRolesUseCase listRolesUseCase;
    @MockBean
    ListRoleFeaturesUseCase listRoleFeaturesUseCase;
    @MockBean
    TokenVerifier tokenVerifier;

    @Test
    void e1_listaModulosDevuelve200() throws Exception {
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(UUID.randomUUID());
        when(listModulesUseCase.listModules()).thenReturn(List.of(
                new RbacModule(UUID.randomUUID(), "SCHEDULES", "Horarios", null, (short) 1, "calendar")));

        mvc.perform(get("/modules").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SCHEDULES"));
    }

    @Test
    void e1_listaRolesDevuelve200() throws Exception {
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(UUID.randomUUID());
        when(listRolesUseCase.listRoles()).thenReturn(List.of(
                new RbacRole(UUID.randomUUID(), "COORDINATOR", "Coordinador", null, false)));

        mvc.perform(get("/roles").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("COORDINATOR"));
    }

    @Test
    void e3_listaCaracteristicasDeUnRolDevuelve200() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(UUID.randomUUID());
        when(listRoleFeaturesUseCase.listRoleFeatures(roleId)).thenReturn(List.of(
                new RoleFeatureGrant(UUID.randomUUID(), "SCHEDULES_WRITE", "Editar horarios", "WRITE", "TRAINING_CENTER")));

        mvc.perform(get("/roles/{roleId}/features", roleId).header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scopeType").value("TRAINING_CENTER"));
    }

    @Test
    void rolInexistenteDevuelve404() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(UUID.randomUUID());
        when(listRoleFeaturesUseCase.listRoleFeatures(roleId)).thenThrow(new RoleNotFoundException());

        mvc.perform(get("/roles/{roleId}/features", roleId).header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_FOUND"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/modules")).andExpect(status().isUnauthorized());
    }
}
