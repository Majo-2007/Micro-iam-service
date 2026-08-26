package co.sena.iam.adapter.in.rest.dto;

import co.sena.iam.domain.model.RbacModule;

import java.util.UUID;

public record ModuleResponse(UUID id, String code, String name, String description,
                              short displayOrder, String iconKey) {
    public static ModuleResponse from(RbacModule m) {
        return new ModuleResponse(m.id(), m.code(), m.name(), m.description(), m.displayOrder(), m.iconKey());
    }
}
