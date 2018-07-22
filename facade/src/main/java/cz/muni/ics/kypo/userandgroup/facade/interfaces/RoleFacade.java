package cz.muni.ics.kypo.userandgroup.facade.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.domain.Pageable;

public interface RoleFacade {

    /**
     * Creates Role with information from given newRoleDTO and returns created role.
     *
     * @param newRoleDTO role to be created
     * @return created role
     */
    RoleDTO createRole(NewRoleDTO newRoleDTO);


    /**
     * Deletes role with given id.
     *
     * @param id id of role to be deleted
     */
    void deleteRole(Long id);

    /**
     * Returns role with given id.
     *
     * @param id of role to be loaded
     * @return loaded role
     */
    RoleDTO getById(Long id);

    /**
     * Returns page of roles with given role type.
     *
     * @param roleType role type of roles to be loaded.
     * @return page of roles with specified role type
     */
    RoleDTO getByRoleType(RoleType roleType);

    /**
     * Returns page of roles specified by given predicate and pageable
     *
     * @param predicate specifies query to databse
     * @param pageable parameter with information about pagination
     * @return page of roles specified by given predicate and pageable
     */
    PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable);


}
