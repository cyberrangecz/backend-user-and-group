package cz.muni.ics.kypo.userandgroup.api.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;

/**
 * The interface for the Role facade layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface RoleFacade {

    /**
     * Returns role with the given ID.
     *
     * @param roleId the ID of the role to be loaded.
     * @return the role encapsulated in {@link RoleDTO}.
     * @throws UserAndGroupFacadeException if the role with the given ID could not be found.
     */
    RoleDTO getById(Long roleId);

    /**
     * Returns role with given role type.
     *
     * @param roleType role type of the role to be loaded.
     * @return the role encapsulated in {@link RoleDTO}.
     * @throws UserAndGroupFacadeException if the role with given role type could not be found.
     */
    RoleDTO getByRoleType(String roleType);

    /**
     * Gets a page of roles specified by the given predicate and pageable.
     *
     * @param predicate specifies query to database.
     * @param pageable pageable parameter with information about pagination.
     * @return page of roles wrapped up in {@link PageResultResource}.
     */
    PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable);


}
