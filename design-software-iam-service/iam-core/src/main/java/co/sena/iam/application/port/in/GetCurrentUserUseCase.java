package co.sena.iam.application.port.in;

import co.sena.iam.domain.model.User;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    /** E4: GET /auth/me */
    User me(UUID userId);
}
