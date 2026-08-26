package co.sena.iam.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "role", schema = "rbac")
public class RoleJpaEntity {

    @Id
    private UUID id;

    private String name;

    @Column(name = "display_name")
    private String displayName;

    private String description;

    @Column(name = "is_system_role")
    private boolean systemRole;

    protected RoleJpaEntity() {
        // JPA
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isSystemRole() { return systemRole; }
}
