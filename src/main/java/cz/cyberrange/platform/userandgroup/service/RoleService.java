package cz.cyberrange.platform.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Business logic for creating and querying roles. */
@Service
public class RoleService {

  private final RoleRepository roleRepository;

  @Autowired
  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  /**
   * Returns the role with the given id.
   *
   * @param id id of the role
   * @return the matching role
   * @throws EntityNotFoundException when no role has that id
   */
  public Role getRoleById(Long id) {
    return roleRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(Role.class, "id", id.getClass(), id)));
  }

  /**
   * Returns the role with the given role type.
   *
   * @param roleType role type to match
   * @return the matching role
   * @throws EntityNotFoundException when no role has that type
   */
  public Role getByRoleType(String roleType) {
    return roleRepository
        .findByRoleType(roleType)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(Role.class, "roleType", roleType.getClass(), roleType)));
  }

  /**
   * Returns the role of the microservice with the given name that belongs to the default group.
   *
   * @param microserviceName name of the microservice
   * @return the matching role
   * @throws EntityNotFoundException when no role of that microservice belongs to the default group
   */
  public Role getDefaultRoleOfMicroservice(String microserviceName) {
    return roleRepository
        .findDefaultRoleOfMicroservice(microserviceName)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        Role.class,
                        "microserviceName",
                        microserviceName.getClass(),
                        microserviceName,
                        "Default role of microservice could not be found")));
  }

  /**
   * Returns every role that matches the given predicate.
   *
   * @param predicate filter applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
    return roleRepository.findAll(predicate, pageable);
  }

  /**
   * Returns every role not assigned to the group with the given id, matching the given predicate.
   *
   * @param groupId id of the group whose roles are excluded
   * @param predicate optional filter further applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  public Page<Role> getAllRolesNotInGivenGroup(
      Long groupId, Predicate predicate, Pageable pageable) {
    return roleRepository.rolesNotInGivenGroup(groupId, predicate, pageable);
  }

  /**
   * Creates the given role.
   *
   * @param role role to create
   * @throws EntityConflictException when a role with that role type already exists
   */
  public void createRole(Role role) {
    if (roleRepository.existsByRoleType(role.getRoleType())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              Role.class,
              "roleType",
              role.getRoleType().getClass(),
              role.getRoleType(),
              "Role already exist. " + "Please name the role with different role type."));
    }
    roleRepository.save(role);
  }

  /**
   * Returns every role belonging to the microservice with the given name.
   *
   * @param nameOfMicroservice name of the microservice
   * @return the matching roles; empty set when none match
   */
  public Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice) {
    return roleRepository.getAllRolesByMicroserviceName(nameOfMicroservice);
  }
}
