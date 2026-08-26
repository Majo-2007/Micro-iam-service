package co.sena.iam.domain.model;

import java.util.UUID;

/** rbac.role: rol asignable a un usuario (ej. "COORDINATOR", "INSTRUCTOR"). */
public record RbacRole(UUID id, String name, String displayName, String description, boolean systemRole) {}
