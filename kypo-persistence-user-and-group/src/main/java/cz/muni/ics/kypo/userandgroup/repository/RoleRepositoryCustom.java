package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface RoleRepositoryCustom {

    /**
     * Find all roles of the given user ID.
     *
     * @param id the ID of the User whose roles are to be found.
     * @return list of {@link Role}s wrapped up in {@link Page}.
     */
    Page<Role> findAllOfUser(Long id, Pageable pageable, Predicate predicate);

    /**
     * Find all roles of the given group ID.
     *
     * @param id the ID of the Group whose roles are to be found.
     * @return list of {@link Role}s wrapped up in {@link Page}.
     */
    Page<Role> findAllOfGroup(Long id, Pageable pageable, Predicate predicate);
}
