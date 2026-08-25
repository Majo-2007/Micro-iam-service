package co.sena.iam.adapter.in.rest;

import co.sena.iam.adapter.in.rest.dto.SessionResponse;
import co.sena.iam.application.port.in.ListUserSessionsUseCase;
import co.sena.iam.application.port.in.RevokeUserSessionUseCase;
import co.sena.iam.application.port.out.TokenVerifier;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * HU-IAM-002 E3: GET /users/{id}/sessions, DELETE /users/{id}/sessions/{sid}.
 * Autorización mínima por ahora: solo el propio usuario puede ver/revocar sus sesiones
 * (el caso "admin gestiona sesiones de otro usuario" es autorización por rol -> HU-IAM-006).
 */
@RestController
@RequestMapping("/users/{userId}/sessions")
public class SessionController {

    private final ListUserSessionsUseCase listUserSessionsUseCase;
    private final RevokeUserSessionUseCase revokeUserSessionUseCase;
    private final TokenVerifier tokenVerifier;

    public SessionController(ListUserSessionsUseCase listUserSessionsUseCase,
                              RevokeUserSessionUseCase revokeUserSessionUseCase,
                              TokenVerifier tokenVerifier) {
        this.listUserSessionsUseCase = listUserSessionsUseCase;
        this.revokeUserSessionUseCase = revokeUserSessionUseCase;
        this.tokenVerifier = tokenVerifier;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> list(@PathVariable UUID userId,
                                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireSameUser(authHeader, userId);
        var sessions = listUserSessionsUseCase.listActiveSessions(userId).stream()
                .map(SessionResponse::from)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID userId, @PathVariable UUID sessionId,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireSameUser(authHeader, userId);
        revokeUserSessionUseCase.revokeSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    private void requireSameUser(String authHeader, UUID pathUserId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        UUID callerId = tokenVerifier.verifyAndGetUserId(authHeader.substring("Bearer ".length()));
        if (!callerId.equals(pathUserId)) {
            throw new InvalidCredentialsException();
        }
    }
}
