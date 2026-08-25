package co.sena.iam.application.port.in;

import java.util.UUID;

public interface RevokeUserSessionUseCase {
    /** E3: DELETE /users/{id}/sessions/{sid} */
    void revokeSession(UUID userId, UUID sessionId);
}
