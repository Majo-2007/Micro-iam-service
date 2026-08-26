package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "feature", schema = "rbac_catalog")
public class FeatureJpaEntity {

    @Id
    private UUID id;

    @Column(name = "module_id")
    private UUID moduleId;

    private String code;
    private String name;
    private String description;

    @Column(name = "action_level")
    private String actionLevel;

    @Column(name = "is_active")
    private boolean active;

    protected FeatureJpaEntity() {
        // JPA
    }

    public UUID getId() { return id; }
    public UUID getModuleId() { return moduleId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getActionLevel() { return actionLevel; }
    public boolean isActive() { return active; }
}
