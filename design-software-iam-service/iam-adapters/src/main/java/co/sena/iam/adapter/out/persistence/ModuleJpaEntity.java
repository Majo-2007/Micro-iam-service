package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "module", schema = "rbac_catalog")
public class ModuleJpaEntity {

    @Id
    private UUID id;
    private String code;
    private String name;
    private String description;

    @Column(name = "display_order")
    private short displayOrder;

    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "is_active")
    private boolean active;

    protected ModuleJpaEntity() {
        // JPA
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public short getDisplayOrder() { return displayOrder; }
    public String getIconKey() { return iconKey; }
    public boolean isActive() { return active; }
}
