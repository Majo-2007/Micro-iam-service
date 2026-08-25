package co.sena.iam.adapter.in.rest;

import co.sena.iam.adapter.in.rest.dto.LoginRequest;
import co.sena.iam.adapter.in.rest.dto.LoginResponse;
import co.sena.iam.adapter.in.rest.dto.LogoutRequest;
import co.sena.iam.adapter.in.rest.dto.MeResponse;
import co.sena.iam.adapter.in.rest.dto.RefreshRequest;
import co.sena.iam.adapter.in.rest.dto.RefreshResponse;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.LoginUseCase;
import co.sena.iam.application.port.in.LoginUseCase.LoginCommand;
import co.sena.iam.application.port.in.LogoutUseCase;
import co.sena.iam.application.port.in.RefreshAccessTokenUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HU-IAM-001: POST /auth/login, GET /auth/me. HU-IAM-002: POST /auth/refresh, POST /auth/logout. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final TokenVerifier tokenVerifier;

    public AuthController(LoginUseCase loginUseCase,
                           GetCurrentUserUseCase getCurrentUserUseCase,
                           RefreshAccessTokenUseCase refreshAccessTokenUseCase,
                           LogoutUseCase logoutUseCase,
                           TokenVerifier tokenVerifier) {
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.tokenVerifier = tokenVerifier;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        var result = loginUseCase.login(new LoginCommand(
                request.email(), request.password(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresInSeconds()));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        var userId = requireCallerId(authHeader);
        return ResponseEntity.ok(MeResponse.from(getCurrentUserUseCase.me(userId)));
    }

    /** E1: nuevo access_token a partir de un refresh_token válido; el refresh no se rota. */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var result = refreshAccessTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(result.accessToken(), result.tokenType(), result.expiresInSeconds()));
    }

    /** E2: revoca el refresh_token recibido. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private java.util.UUID requireCallerId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        return tokenVerifier.verifyAndGetUserId(authHeader.substring("Bearer ".length()));
    }
}
