package co.sena.iam.application.port.in;

import co.sena.iam.domain.model.RefreshToken;

import java.util.List;
import java.util.UUID;

public interface ListUserSessionsUseCase {
    /** E3: GET /users/{id}/sessions */
    List<RefreshToken> listActiveSessions(UUID userId);
}
