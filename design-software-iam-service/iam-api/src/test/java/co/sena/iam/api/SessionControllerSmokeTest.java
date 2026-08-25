package co.sena.iam.api;

import co.sena.iam.adapter.in.rest.GlobalExceptionHandler;
import co.sena.iam.adapter.in.rest.SessionController;
import co.sena.iam.application.port.in.ListUserSessionsUseCase;
import co.sena.iam.application.port.in.RevokeUserSessionUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.SessionNotFoundException;
import co.sena.iam.domain.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU-IAM-002 E3: GET/DELETE /users/{id}/sessions. */
@WebMvcTest(controllers = SessionController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class SessionControllerSmokeTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ListUserSessionsUseCase listUserSessionsUseCase;
    @MockBean
    RevokeUserSessionUseCase revokeUserSessionUseCase;
    @MockBean
    TokenVerifier tokenVerifier;

    @Test
    void e3_listarSesionesPropiasDevuelve200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(userId);
        when(listUserSessionsUseCase.listActiveSessions(userId)).thenReturn(List.of(
                new RefreshToken(sessionId, userId, "hash", "chrome", "127.0.0.1",
                        Instant.parse("2026-08-24T12:00:00Z"), Instant.parse("2026-08-31T12:00:00Z"), false, null)
        ));

        mvc.perform(get("/users/{userId}/sessions", userId).header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sessionId.toString()))
                .andExpect(jsonPath("$[0].deviceHint").value("chrome"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(get("/users/{userId}/sessions", userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarSesionesDeOtroUsuarioDevuelve401() throws Exception {
        UUID caller = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(caller);

        mvc.perform(get("/users/{userId}/sessions", otherUser).header("Authorization", "Bearer valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void e3_revocarSesionPropiaDevuelve204() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(userId);

        mvc.perform(delete("/users/{userId}/sessions/{sessionId}", userId, sessionId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void revocarSesionInexistenteDevuelve404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(tokenVerifier.verifyAndGetUserId(any())).thenReturn(userId);
        org.mockito.Mockito.doThrow(new SessionNotFoundException())
                .when(revokeUserSessionUseCase).revokeSession(eq(userId), eq(sessionId));

        mvc.perform(delete("/users/{userId}/sessions/{sessionId}", userId, sessionId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    }
}
