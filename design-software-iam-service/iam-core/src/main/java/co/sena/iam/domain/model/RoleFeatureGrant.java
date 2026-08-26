package co.sena.iam.domain.model;

import java.util.UUID;

/**
 * rbac.role_feature: qué característica otorga un rol y con qué alcance (scope_type).
 * El alcance real (ej. a qué centro de formación aplica) se fija por asignación en
 * rbac.user_role.training_center_id (HU-IAM-005) — aquí solo se describe el TIPO de alcance
 * que ese rol concede para esa característica (GLOBAL, TRAINING_CENTER, OWN_FICHAS, etc.).
 */
public record RoleFeatureGrant(UUID featureId, String featureCode, String featureName,
                                String actionLevel, String scopeType) {}
