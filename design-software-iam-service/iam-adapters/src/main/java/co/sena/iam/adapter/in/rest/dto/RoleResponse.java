package co.sena.iam.adapter.in.rest.dto;

import co.sena.iam.domain.model.RbacRole;

import java.util.UUID;

public record RoleResponse(UUID id, String name, String displayName, String description, boolean systemRole) {
    public static RoleResponse from(RbacRole r) {
        return new RoleResponse(r.id(), r.name(), r.displayName(), r.description(), r.systemRole());
    }
}
