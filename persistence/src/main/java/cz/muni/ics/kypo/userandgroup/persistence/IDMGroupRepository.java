package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long> {

    IDMGroup findByName(String name);

    List<IDMGroup> findAllByName(String name);

}
