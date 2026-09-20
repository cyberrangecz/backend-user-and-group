package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.MicroserviceMapper;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalWO;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.MicroserviceService;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Operations for registering microservices, along with the roles each one defines, and for querying
 * microservices already registered.
 */
@Service
public class MicroserviceFacade {

  private final MicroserviceService microserviceService;
  private final RoleService roleService;
  private final IDMGroupService groupService;
  private final MicroserviceMapper microserviceMapper;
  private final RoleMapper roleMapper;

  @Autowired
  public MicroserviceFacade(
      MicroserviceService microserviceService,
      RoleService roleService,
      IDMGroupService groupService,
      MicroserviceMapper microserviceMapper,
      RoleMapper roleMapper) {
    this.microserviceService = microserviceService;
    this.roleService = roleService;
    this.groupService = groupService;
    this.microserviceMapper = microserviceMapper;
    this.roleMapper = roleMapper;
  }

  /**
   * Returns every microservice that matches the given predicate.
   *
   * @param predicate filter applied to the microservices
   * @param pageable page and sort request
   * @return the matching page of microservices; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<MicroserviceDTO> getAllMicroservices(
      Predicate predicate, Pageable pageable) {
    return microserviceMapper.mapToPageResultResource(
        microserviceService.getMicroservices(predicate, pageable));
  }

  /**
   * Registers the given microservice together with its roles. When a microservice with that name
   * already exists, its endpoint is updated and its roles are reconciled instead of creating a new
   * microservice: each given role not already defined for it is added, and a role flagged as
   * default replaces the microservice's previous default role in the group that holds every
   * microservice's default role. For a microservice registered for the first time, each given role
   * is added directly, and a role flagged as default is added to that same group.
   *
   * @param newMicroserviceDTO microservice to register, together with its roles
   * @throws UnprocessableEntityException when more than one of the given roles is flagged as
   *     default
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     a role with the same role type as one of the given roles already exists
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     an existing microservice being given a new default role has no default role of its own to
   *     replace
   */
  @TransactionalWO
  public void registerMicroservice(NewMicroserviceDTO newMicroserviceDTO) {
    Microservice microservice = microserviceMapper.mapCreateToEntity(newMicroserviceDTO);
    if (microserviceService.existsByName(newMicroserviceDTO.getName())) {
      // microservice already exists, update roles of microservice
      microservice = microserviceService.getMicroserviceByName(newMicroserviceDTO.getName());
      microservice.setEndpoint(newMicroserviceDTO.getEndpoint());
      updateRolesOfMicroservice(newMicroserviceDTO.getRoles(), microservice);
    } else {
      // microservice does not exist
      microserviceService.createMicroservice(microservice);
      createNewRolesOfMicroservice(newMicroserviceDTO.getRoles(), microservice);
    }
  }

  private void createNewRolesOfMicroservice(
      Set<RoleForNewMicroserviceDTO> newRolesDTO, Microservice microservice) {
    checkDefaultRole(newRolesDTO);
    newRolesDTO.forEach(
        newRole -> {
          Role role = roleMapper.mapToEntity(newRole);
          role.setMicroservice(microservice);
          // maybe remove try-catch and let conflict exception bubble up
          roleService.createRole(role);
          if (newRole.isDefault()) {
            IDMGroup defaultGroup = groupService.getGroupForDefaultRoles();
            defaultGroup.addRole(role);
          }
        });
  }

  private void updateRolesOfMicroservice(
      Set<RoleForNewMicroserviceDTO> newRolesDTO, Microservice microservice) {
    checkDefaultRole(newRolesDTO);
    Set<Role> rolesInDB = roleService.getAllRolesOfMicroservice(microservice.getName());
    newRolesDTO.forEach(
        newRole -> {
          Role role = roleMapper.mapToEntity(newRole);
          role.setMicroservice(microservice);
          // if role is already in microservice it returns false, otherwise it returns true and the
          // newly created role will be added
          if (rolesInDB.add(role)) {
            roleService.createRole(role);
            if (newRole.isDefault()) {
              IDMGroup defaultGroup = groupService.getGroupForDefaultRoles();
              // since it is not possible to have two default roles for particular microservice the
              // old one is removed from the default group
              // repair since microservice do not have to default role
              defaultGroup.removeRole(
                  roleService.getDefaultRoleOfMicroservice(microservice.getName()));
              defaultGroup.addRole(role);
            }
          }
        });
  }

  private void checkDefaultRole(Set<RoleForNewMicroserviceDTO> rolesToCheck) {
    if (rolesToCheck.stream().filter(RoleForNewMicroserviceDTO::isDefault).count() > 1) {
      throw new UnprocessableEntityException(
          new EntityErrorDetail(
              Microservice.class,
              "Microservice which you are trying to register cannot have more than 1 default role."));
    }
  }
}
