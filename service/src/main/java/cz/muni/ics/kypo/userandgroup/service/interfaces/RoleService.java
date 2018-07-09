package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;

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
     * @throws IdentityManagementException if role was not found
     */
    Role getById(Long id) throws IdentityManagementException;

    /**
     * Return role by role type
     *
     * @param roleType of role
     * @return role with given roleType
     */
    Role getByRoleType(String roleType);

    /**
     * Returns all roles
     *
     * @return all roles
     */
    List<Role> getAllRoles();
}
