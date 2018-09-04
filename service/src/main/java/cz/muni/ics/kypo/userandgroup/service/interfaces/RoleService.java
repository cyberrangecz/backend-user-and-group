package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    /**
     * Returns role by id
     *
     * @param id of role
     * @return role with given id
     * @throws UserAndGroupServiceException if role could not be found
     */
    Role getById(Long id) throws UserAndGroupServiceException;

    /**
     * Return role by role type
     *
     * @param roleType of role
     * @return role with given roleType
     * @throws UserAndGroupServiceException when role with given role type could not be found
     */
    Role getByRoleType(RoleType roleType) throws UserAndGroupServiceException;

    /**
     * Returns all roles
     *
     * @return all roles
     */
    Page<Role> getAllRoles(Pageable pageable);
}
