package co.sena.iam.adapter.in.rest.dto;

import co.sena.iam.domain.model.User;

import java.util.UUID;

public record MeResponse(UUID id, String email, String firstName, String lastName, String actorType) {
    public static MeResponse from(User user) {
        return new MeResponse(user.id(), user.email(), user.firstName(), user.lastName(), user.actorType());
    }
}
