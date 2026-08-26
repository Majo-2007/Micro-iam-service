package co.sena.iam.adapter.in.rest.dto;

import co.sena.iam.domain.model.RoleFeatureGrant;

import java.util.UUID;

public record RoleFeatureResponse(UUID featureId, String featureCode, String featureName,
                                   String actionLevel, String scopeType) {
    public static RoleFeatureResponse from(RoleFeatureGrant g) {
        return new RoleFeatureResponse(g.featureId(), g.featureCode(), g.featureName(), g.actionLevel(), g.scopeType());
    }
}
