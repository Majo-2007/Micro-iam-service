package co.sena.iam.api;

import co.sena.iam.adapter.in.rest.AuthController;
import co.sena.iam.adapter.in.rest.GlobalExceptionHandler;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.LoginUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.AccountLockedException;
import co.sena.iam.domain.exception.InvalidCredentialsException;
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

/** HU-IAM-001: smoke test de la capa REST (E1/E2/E3) con los casos de uso mockeados. */
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
    TokenVerifier tokenVerifier;

    @Test
    void e1_loginOkDevuelve200ConToken() throws Exception {
        when(loginUseCase.login(any())).thenReturn(
                new LoginUseCase.LoginResult("fake-jwt", "Bearer", 900));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@sena.co\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("fake-jwt"))
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
}
