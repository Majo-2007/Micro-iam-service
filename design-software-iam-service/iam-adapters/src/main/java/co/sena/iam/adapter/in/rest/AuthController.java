package co.sena.iam.adapter.in.rest;

import co.sena.iam.adapter.in.rest.dto.LoginRequest;
import co.sena.iam.adapter.in.rest.dto.LoginResponse;
import co.sena.iam.adapter.in.rest.dto.MeResponse;
import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.in.LoginUseCase;
import co.sena.iam.application.port.in.LoginUseCase.LoginCommand;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HU-IAM-001: POST /auth/login, GET /auth/me. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final TokenVerifier tokenVerifier;

    public AuthController(LoginUseCase loginUseCase,
                           GetCurrentUserUseCase getCurrentUserUseCase,
                           TokenVerifier tokenVerifier) {
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.tokenVerifier = tokenVerifier;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        var result = loginUseCase.login(new LoginCommand(
                request.email(), request.password(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(new LoginResponse(result.accessToken(), result.tokenType(), result.expiresInSeconds()));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        var userId = tokenVerifier.verifyAndGetUserId(authHeader.substring("Bearer ".length()));
        return ResponseEntity.ok(MeResponse.from(getCurrentUserUseCase.me(userId)));
    }
}
