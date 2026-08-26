package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "role_feature", schema = "rbac")
public class RoleFeatureJpaEntity {

    @Id
    private UUID id;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    // @ManyToOne (no solo el UUID) para poder traer code/name/actionLevel de la característica
    // en una sola query (JOIN FETCH) y evitar N+1 / LazyInitializationException.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureJpaEntity feature;

    @Column(name = "scope_type", nullable = false)
    private String scopeType;

    protected RoleFeatureJpaEntity() {
        // JPA
    }

    public UUID getId() { return id; }
    public UUID getRoleId() { return roleId; }
    public FeatureJpaEntity getFeature() { return feature; }
    public String getScopeType() { return scopeType; }
}
