package cz.muni.ics.kypo.userandgroup.api.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.RoleTypeDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;

public interface RoleFacade {

    /**
     * Returns role with given id.
     *
     * @param id of role to be loaded
     * @return loaded role
     * @throws UserAndGroupFacadeException if role with given id could not be found
     */
    RoleDTO getById(Long id);

    /**
     * Returns role with given role type.
     *
     * @param roleType role type of role to be loaded.
     * @return page of roles with specified role type
     * @throws UserAndGroupFacadeException if role with given role type could not be found
     */
    RoleDTO getByRoleType(String roleType);

    /**
     * Returns page of roles specified by given predicate and pageable
     *
     * @param pageable parameter with information about pagination
     * @return page of roles specified by given predicate and pageable
     * @throws UserAndGroupFacadeException if some of microservice does not return http code 2xx
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable);


}
