package co.sena.iam.domain.model;

import java.util.UUID;

/** rbac_catalog.module: agrupador de funcionalidades del sistema (ej. "Fichas", "Horarios"). */
public record RbacModule(UUID id, String code, String name, String description,
                          short displayOrder, String iconKey) {}
