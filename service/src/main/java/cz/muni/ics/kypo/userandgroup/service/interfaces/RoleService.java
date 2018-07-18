package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    /**
     * Creates given role
     *
     * @param role to be created
     * @return created role
     */
    Role create(Role role);

    /**
     * Deletes given role
     * @param role to be deleted
     */
    void delete(Role role);

    /**
     * Returns role by id
     *
     * @param id of role
     * @return role with given id
     * @throws IdentityManagementException if role could not be found
     */
    Role getById(Long id) throws IdentityManagementException;

    /**
     * Return role by role type
     *
     * @param roleType of role
     * @return role with given roleType
     * @throws IdentityManagementException when role with given role type could not be found
     */
    Role getByRoleType(String roleType) throws IdentityManagementException;

    /**
     * Returns all roles
     *
     * @return all roles
     */
    Page<Role> getAllRoles(Pageable pageable);
}
