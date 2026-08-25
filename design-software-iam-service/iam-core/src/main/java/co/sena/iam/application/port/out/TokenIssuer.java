package co.sena.iam.application.port.out;

import co.sena.iam.domain.model.User;

public interface TokenIssuer {
    /** HU-IAM-010: emite el access_token (15 min) con sub/roles/scopes/exp. */
    IssuedToken issueAccessToken(User user);

    record IssuedToken(String value, long expiresInSeconds) {}
}
