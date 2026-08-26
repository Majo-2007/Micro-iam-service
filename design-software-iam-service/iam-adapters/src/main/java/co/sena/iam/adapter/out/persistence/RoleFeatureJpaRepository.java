package co.sena.iam.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleFeatureJpaRepository extends JpaRepository<RoleFeatureJpaEntity, UUID> {

    // JOIN FETCH explícito: trae la característica en la misma consulta, sin depender
    // de si open-in-view está activo o no (aquí está desactivado a propósito).
    @Query("SELECT rf FROM RoleFeatureJpaEntity rf JOIN FETCH rf.feature WHERE rf.roleId = :roleId")
    List<RoleFeatureJpaEntity> findByRoleIdWithFeature(@Param("roleId") UUID roleId);
}
