package co.sena.iam.api;

import co.sena.iam.adapter.in.rest.AuthController;
import co.sena.iam.adapter.in.rest.GlobalExceptionHandler;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.LoginUseCase;
import co.sena.iam.application.port.in.LogoutUseCase;
import co.sena.iam.application.port.in.RefreshAccessTokenUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.exception.TokenRevokedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-IAM-001: smoke test de /auth/login, /auth/me (E1/E2/E3).
 * HU-IAM-002: smoke test de /auth/refresh, /auth/logout (E1/E2). Casos de uso mockeados.
 */
@WebMvcTest(controllers = AuthController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class IamApiApplicationSmokeTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    LoginUseCase loginUseCase;
    @MockBean
    GetCurrentUserUseCase getCurrentUserUseCase;
    @MockBean
    RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    @MockBean
    LogoutUseCase logoutUseCase;
    @MockBean
    TokenVerifier tokenVerifier;

    @Test
    void e1_loginOkDevuelve200ConAccessYRefreshToken() throws Exception {
        when(loginUseCase.login(any())).thenReturn(
                new LoginUseCase.LoginResult("fake-jwt", "fake-refresh", "Bearer", 900));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@sena.co\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("fake-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("fake-refresh"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void e2_credencialesInvalidasDevuelve401() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@sena.co\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void e3_cuentaBloqueadaDevuelve423() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new AccountLockedException(Instant.parse("2026-08-24T12:15:00Z")));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@sena.co\",\"password\":\"secret123\"}"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void hu002E1_refreshValidoDevuelve200ConNuevoAccessToken() throws Exception {
        when(refreshAccessTokenUseCase.refresh(any())).thenReturn(
                new RefreshAccessTokenUseCase.AccessTokenResult("new-access", "Bearer", 900));

        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void hu002E2_refreshRevocadoDevuelve401TokenRevoked() throws Exception {
        when(refreshAccessTokenUseCase.refresh(any())).thenThrow(new TokenRevokedException());

        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"revoked-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_REVOKED"));
    }

    @Test
    void hu002E2_logoutOkDevuelve204() throws Exception {
        mvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-refresh-token\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void hu002E2_reusarLogoutDevuelve401TokenRevoked() throws Exception {
        org.mockito.Mockito.doThrow(new TokenRevokedException()).when(logoutUseCase).logout(any());

        mvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"already-revoked\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_REVOKED"));
    }
}
