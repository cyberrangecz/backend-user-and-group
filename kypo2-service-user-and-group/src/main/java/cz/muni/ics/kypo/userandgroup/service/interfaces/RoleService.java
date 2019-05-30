package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
    Role getByRoleType(String roleType) throws UserAndGroupServiceException;

    /**
     * Returns all roles
     *
     * @return all roles
     */
    Page<Role> getAllRoles(Predicate predicate, Pageable pageable);

    /**
     * Create new role
     *
     * @param role to be created
     * @throws UserAndGroupServiceException if role with given role type already exist
     */
    void create(Role role);

    /**
     * Returns all role of given microservice
     *
     * @param nameOfMicroservice of which get roles
     * @return role of microservice with given name
     */
    Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice);
}
