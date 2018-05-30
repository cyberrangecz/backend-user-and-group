package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long> {

    IDMGroup findByName(String name);

    List<IDMGroup> findAllByName(String name);

    @Query("SELECT g FROM IDMGroup AS g LEFT JOIN g.roles AS r WHERE r.roleType = 'ADMINISTRATOR'")
    IDMGroup findAdministratorGroup();

    @Query("SELECT g FROM IDMGroup AS g LEFT JOIN g.roles AS r WHERE r.roleType = 'USER'")
    IDMGroup findUserGroup();

    @Query("SELECT g FROM IDMGroup AS g LEFT JOIN g.roles AS r WHERE r.roleType = 'GUEST'")
    IDMGroup findGuestGroup();

}
