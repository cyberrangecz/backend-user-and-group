package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;


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

    /**
     * Find all roles, not in the given {@link IDMGroup} ID.
     *
     * @param groupId   unique identifier of the group whose roles will be omitted.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  abstract interface for pagination information.
     * @return returns list of all {@link Role}s except those in {@link IDMGroup}
     * with given ID wrapped in {@link Page}
     */
    Page<Role> rolesNotInGivenGroup(@Param("groupId") Long groupId, Predicate predicate, Pageable pageable);

}
