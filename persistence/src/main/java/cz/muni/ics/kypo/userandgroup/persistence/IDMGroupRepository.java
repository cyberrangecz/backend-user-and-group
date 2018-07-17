package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long> {

    Optional<IDMGroup> findByName(String name);

    List<IDMGroup> findAllByName(String name);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = 'ADMINISTRATOR'")
    Optional<IDMGroup> findAdministratorGroup();

    @Query("SELECT r FROM IDMGroup g INNER JOIN g.roles r WHERE g.id = :groupId")
    Set<Role> getRolesOfGroup(@Param("groupId") Long id);

    @EntityGraph(value = "IDMGroup.users", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT g FROM IDMGroup g WHERE g.name = :name")
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);


}
