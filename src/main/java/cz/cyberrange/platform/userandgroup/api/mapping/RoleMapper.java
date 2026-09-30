package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts between {@link Role} entities and role DTOs, in both directions and across collections
 * and pages.
 */
@Mapper(
    componentModel = "spring",
    uses = {MicroserviceMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper extends ParentMapper {

  /**
   * Maps a role DTO to a role entity. The microservice reference is left unset.
   *
   * @param dto role to map
   * @return the mapped role
   */
  Role mapToEntity(RoleDTO dto);

  /**
   * Maps a new role definition to a role entity. The id and microservice reference are left unset;
   * the default-role flag has no matching field and is not carried over.
   *
   * @param dto role definition to map
   * @return the mapped role
   */
  Role mapToEntity(RoleForNewMicroserviceDTO dto);

  /**
   * Maps a role entity to a role DTO. The microservice id and name are left unset.
   *
   * @param entity role to map
   * @return the mapped role
   */
  RoleDTO mapToDTO(Role entity);

  /**
   * Maps each role DTO in the given collection to a role entity. Each mapped role has its
   * microservice reference left unset.
   *
   * @param dtos roles to map
   * @return the mapped roles, in a new list
   */
  List<Role> mapToList(Collection<RoleDTO> dtos);

  /**
   * Maps each role entity in the given collection to a role DTO. Each mapped role has its
   * microservice id and name left unset.
   *
   * @param entities roles to map
   * @return the mapped roles, in a new list
   */
  List<RoleDTO> mapToListDTO(Collection<Role> entities);

  /**
   * Maps each role DTO in the given collection to a role entity. Each mapped role has its
   * microservice reference left unset.
   *
   * @param dtos roles to map
   * @return the mapped roles, in a new set
   */
  Set<Role> mapToSet(Collection<RoleDTO> dtos);

  /**
   * Maps each new role definition in the given collection to a role entity. Each mapped role has
   * its id and microservice reference left unset.
   *
   * @param dtos role definitions to map
   * @return the mapped roles, in a new set
   */
  Set<Role> mapToSetOfNewRoles(Collection<RoleForNewMicroserviceDTO> dtos);

  /**
   * Maps each role entity in the given collection to a role DTO, with the microservice id and name
   * of each role filled in.
   *
   * @param entities roles to map
   * @return the mapped roles, in a new set
   */
  @IterableMapping(qualifiedByName = "roleToRoleDTOWithMicroservice")
  Set<RoleDTO> mapToSetDTO(Collection<Role> entities);

  /**
   * Maps a role entity to a role DTO with its microservice id and name filled in.
   *
   * @param entity role to map
   * @return the mapped role, with its microservice id and name included
   */
  @Named("roleToRoleDTOWithMicroservice")
  default RoleDTO mapToRoleDTOWithMicroservice(Role entity) {
    RoleDTO roleDTO = mapToDTO(entity);
    roleDTO.setIdOfMicroservice(entity.getMicroservice().getId());
    roleDTO.setNameOfMicroservice(entity.getMicroservice().getName());
    return roleDTO;
  }

  /**
   * Maps a role DTO to a role entity.
   *
   * @param dto role to map
   * @return the mapped role, empty when dto is null
   */
  default Optional<Role> mapToOptional(RoleDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  /**
   * Maps a role entity to a role DTO.
   *
   * @param entity role to map
   * @return the mapped role, empty when entity is null
   */
  default Optional<RoleDTO> mapToOptional(Role entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  /**
   * Maps a page of role entities to a page of role DTOs, keeping the given page's pageable. The
   * total element count of the returned page is the number of mapped roles, not the original page's
   * total.
   *
   * @param objects page of roles to map
   * @return the mapped page
   */
  default Page<RoleDTO> mapToPageDTO(Page<Role> objects) {
    List<RoleDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of role DTOs to a page of role entities, keeping the given page's pageable. The
   * total element count of the returned page is the number of mapped roles, not the original page's
   * total.
   *
   * @param objects page of role DTOs to map
   * @return the mapped page
   */
  default Page<Role> mapToPage(Page<RoleDTO> objects) {
    List<Role> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of role entities to a paginated result carrying role DTOs, with the microservice id
   * and name of each role left unset. The pagination metadata reflects the original page.
   *
   * @param objects page of roles to map
   * @return the mapped roles together with the page's pagination metadata
   */
  default PageResultResource<RoleDTO> mapToPageResultResource(Page<Role> objects) {
    List<RoleDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
