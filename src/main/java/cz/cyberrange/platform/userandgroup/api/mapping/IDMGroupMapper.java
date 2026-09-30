package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupBaseDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupWithRolesDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts between {@link IDMGroup} entities and group DTOs, in both directions and across
 * collections and pages.
 */
@Mapper(
    componentModel = "spring",
    uses = {RoleMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IDMGroupMapper extends ParentMapper {

  /**
   * Maps a group DTO to a group entity. The status and external id are left unset; the DTO's
   * deletion flag has no matching field and is not carried over.
   *
   * @param dto group to map
   * @return the mapped group
   */
  IDMGroup mapToEntity(GroupDTO dto);

  /**
   * Maps a group entity to a group DTO, with each role's microservice id and name filled in. The
   * deletion flag is false when the group is one of the groups created automatically by the
   * service, and true otherwise.
   *
   * @param entity group to map
   * @return the mapped group
   */
  GroupDTO mapToDTO(IDMGroup entity);

  /**
   * Maps a group entity to a basic group view, without its users and roles. The deletion flag is
   * false when the group is one of the groups created automatically by the service, and true
   * otherwise.
   *
   * @param entity group to map
   * @return the mapped group view
   */
  GroupViewDTO mapToViewDTO(IDMGroup entity);

  /**
   * Maps a group entity to a group DTO including its roles, with each role's microservice id and
   * name filled in. The deletion flag is false when the group is one of the groups created
   * automatically by the service, and true otherwise.
   *
   * @param entity group to map
   * @return the mapped group with its roles
   */
  GroupWithRolesDTO mapToWithRolesDto(IDMGroup entity);

  /**
   * Maps a new group request to a group entity. The id, status, external id and roles are left
   * unset; the list of group ids to import users from has no matching field and is not carried
   * over.
   *
   * @param dto new group data to map
   * @return the mapped group
   */
  IDMGroup mapCreateToEntity(NewGroupDTO dto);

  /**
   * Maps a group update request to a group entity. The status, external id, users and roles are
   * left unset.
   *
   * @param dto group update data to map
   * @return the mapped group
   */
  IDMGroup mapUpdateToEntity(UpdateGroupDTO dto);

  /**
   * Maps each group DTO in the given collection to a group entity. Each mapped group has its status
   * and external id left unset.
   *
   * @param dtos groups to map
   * @return the mapped groups, in a new list
   */
  List<IDMGroup> mapToList(Collection<GroupDTO> dtos);

  /**
   * Maps each group entity in the given collection to a basic group view, without its users and
   * roles. Each view of a group created automatically by the service has its deletion flag set to
   * false, and every other view has it at true.
   *
   * @param entities groups to map
   * @return the mapped group views, in a new list
   */
  List<GroupViewDTO> mapToListDTO(Collection<IDMGroup> entities);

  /**
   * Maps each group DTO in the given collection to a group entity. Each mapped group has its status
   * and external id left unset.
   *
   * @param dtos groups to map
   * @return the mapped groups, in a new set
   */
  Set<IDMGroup> mapToSet(Collection<GroupDTO> dtos);

  /**
   * Maps each group entity in the given collection to a group DTO, with each role's microservice id
   * and name filled in. Each group created automatically by the service has its deletion flag set
   * to false, and every other group has it at true.
   *
   * @param entities groups to map
   * @return the mapped groups, in a new set
   */
  Set<GroupDTO> mapToSetDTO(Collection<IDMGroup> entities);

  /**
   * Maps a group DTO to a group entity.
   *
   * @param dto group to map
   * @return the mapped group, empty when dto is null
   */
  default Optional<IDMGroup> mapToOptional(GroupDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  /**
   * Maps a group entity to a group DTO.
   *
   * @param entity group to map
   * @return the mapped group, empty when entity is null
   */
  default Optional<GroupDTO> mapToOptional(IDMGroup entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  /**
   * Maps a page of group entities to a page of basic group views, keeping the given page's
   * pageable. The total element count of the returned page is the number of mapped groups, not the
   * original page's total.
   *
   * @param objects page of groups to map
   * @return the mapped page
   */
  default Page<GroupViewDTO> mapToPageDTO(Page<IDMGroup> objects) {
    List<GroupViewDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of group entities to a paginated result carrying basic group views. The pagination
   * metadata reflects the original page.
   *
   * @param objects page of groups to map
   * @return the mapped group views together with the page's pagination metadata
   */
  default PageResultResource<GroupViewDTO> mapToPageResultResource(Page<IDMGroup> objects) {
    List<GroupViewDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToViewDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  /**
   * Denies deletion of a mapped group that is one of the groups created automatically by the
   * service, leaving the flag of every other group at the value it was mapped with.
   *
   * @param groupDTO mapped group to apply the rule to
   */
  @AfterMapping
  default void denyDeletionOfImplicitGroup(@MappingTarget GroupBaseDTO groupDTO) {
    if (implicitGroupNames().contains(groupDTO.getName())) {
      groupDTO.setCanBeDeleted(false);
    }
  }

  private static Set<String> implicitGroupNames() {
    return Set.of(
        ImplicitGroupNames.DEFAULT_GROUP.getName(),
        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(),
        ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName());
  }
}
