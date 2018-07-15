package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(value = "Role.childrenRoles", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Role> findByRoleType(String roleType);

    boolean existsByRoleType(String roleType);
}
