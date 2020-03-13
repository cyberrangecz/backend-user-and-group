package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * The interface for the Role service layer.
 */
public interface RoleService {

    /**
     * Returns role by ID.
     *
     * @param id the ID of the role.
     * @return the {@link Role} with the given ID.
     * @throws EntityNotFoundException if the role could not be found.
     */
    Role getRoleById(Long id) throws EntityNotFoundException;

    /**
     * Returns the role by its role type.
     *
     * @param roleType the name of the role.
     * @return the {@link Role} with the given role type.
     * @throws EntityNotFoundException when the role with given role type could not be found.
     */
    Role getByRoleType(String roleType) throws EntityNotFoundException;

    /**
     * Returns the default role of microservice.
     *
     * @param microserviceName the name of the microservice.
     * @return the {@link Role} with the given role type.
     * @throws EntityNotFoundException when the default role of the microservice could not be found.
     */
    Role getDefaultRoleOfMicroservice(String microserviceName) throws EntityNotFoundException;

    /**
     * Returns list of all roles.
     *
     * @param predicate specifies query to database.
     * @param pageable pageable parameter with information about pagination.
     * @return list of all {@link Role}s from database wrapped up in {@link Page}.
     */
    Page<Role> getAllRoles(Predicate predicate, Pageable pageable);

    /**
     * Creates the given role in the database.
     *
     * @param role role to be created.
     * @throws EntityConflictException if given role already exists.
     */
    void createRole(Role role) throws EntityConflictException;

    /**
     * Returns all roles of given {@link cz.muni.ics.kypo.userandgroup.entities.Microservice}
     *
     * @param nameOfMicroservice unique name of microservice for which getGroupById roles.
     * @return set of {@link Role}s of microservice with the given name.
     */
    Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice);
}
