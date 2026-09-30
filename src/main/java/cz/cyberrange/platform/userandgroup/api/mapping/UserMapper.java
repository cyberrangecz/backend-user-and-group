package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserBasicViewDto;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserImportDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserUpdateDTO;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts between the {@link User} entity and its API DTOs, including the paginated and
 * role-augmented variants returned by the user endpoints.
 */
@Mapper(
    componentModel = "spring",
    uses = {RoleMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends ParentMapper {

  /**
   * Maps a user DTO into a user entity. Copies id, sub, iss, fullName, mail, givenName, familyName
   * and picture; the dto's roles are not carried over. externalId stays unset, groups stays empty,
   * and status keeps the value the entity's no-argument constructor assigns.
   *
   * @param dto dto to map
   * @return the mapped entity
   */
  User mapToEntity(UserDTO dto);

  /**
   * Maps a user-creation DTO into a user entity. Copies sub, iss, fullName, givenName, familyName,
   * externalId, mail and picture; id stays unset, groups stays empty, and status keeps the value
   * the entity's no-argument constructor assigns.
   *
   * @param dto dto to map
   * @return the mapped entity
   */
  User mapToEntity(UserCreateDTO dto);

  /**
   * Maps a user-import DTO into a user entity. Copies sub, iss, fullName, givenName and familyName;
   * id, externalId, mail and picture stay unset, groups stays empty, and status keeps the value the
   * entity's no-argument constructor assigns.
   *
   * @param dto dto to map
   * @return the mapped entity
   */
  User mapToEntity(UserImportDTO dto);

  /**
   * Maps a user-update DTO into a user entity. Copies sub, iss, fullName, givenName, familyName,
   * externalId, mail and picture; id stays unset, groups stays empty, and status keeps the value
   * the entity's no-argument constructor assigns.
   *
   * @param dto dto to map
   * @return the mapped entity
   */
  User mapToEntity(UserUpdateDTO dto);

  /**
   * Maps a user DTO into an update DTO. Copies sub, iss, fullName, givenName, familyName, mail and
   * picture; the dto's id and roles are not carried over, and externalId stays unset.
   *
   * @param dto dto to map
   * @return the mapped update DTO
   */
  UserUpdateDTO mapUserDTOToUserUpdateDTO(UserDTO dto);

  /**
   * Maps a role entity into a role DTO. Copies id, roleType and description; idOfMicroservice and
   * nameOfMicroservice stay unset.
   *
   * @param entity entity to map
   * @return the mapped DTO
   */
  RoleDTO mapRoleToDTO(Role entity);

  /**
   * Maps a user entity into a user DTO. Copies id, sub, iss, fullName, mail, givenName, familyName
   * and picture; roles stays the empty set it is declared with.
   *
   * @param entity entity to map
   * @return the mapped DTO
   */
  UserDTO mapToDTO(User entity);

  /**
   * Maps a user entity into its basic view DTO, copying id, sub, iss, fullName, mail, givenName,
   * familyName and picture.
   *
   * @param entity entity to map
   * @return the mapped DTO
   */
  UserBasicViewDto mapToBasicViewDto(User entity);

  /**
   * Maps a group-listing user DTO into a user entity. Copies id, sub, iss, fullName, givenName,
   * familyName, mail and picture; externalId stays unset, groups stays empty, and status keeps the
   * value the entity's no-argument constructor assigns.
   *
   * @param userForGroupsDTO dto to map
   * @return the mapped entity
   */
  User mapUserForGroupsDTOToEntity(UserForGroupsDTO userForGroupsDTO);

  /**
   * Maps a user entity into its group-listing DTO, copying id, sub, iss, fullName, givenName,
   * familyName, mail and picture.
   *
   * @param user entity to map
   * @return the mapped DTO
   */
  UserForGroupsDTO mapEntityToUserForGroupsDTO(User user);

  /**
   * Maps each given user DTO into a user entity, in the same order. Each entity leaves externalId
   * unset, leaves groups empty and keeps status at the value its constructor assigns.
   *
   * @param dtos dtos to map
   * @return the mapped entities, in the order given
   */
  List<User> mapToList(Collection<UserDTO> dtos);

  /**
   * Maps each given user-import DTO into a user entity, collected into a set. Each entity leaves
   * id, externalId, mail and picture unset, leaves groups empty and keeps status at the value its
   * constructor assigns; two dtos with the same sub and issuer collapse into one entity.
   *
   * @param dtos dtos to map
   * @return the mapped entities, one per distinct sub and issuer
   */
  Set<User> mapUsersImportToSet(Collection<UserImportDTO> dtos);

  /**
   * Maps each given user entity into a user DTO, in the same order. Each DTO's roles stays the
   * empty set it is declared with.
   *
   * @param entities entities to map
   * @return the mapped DTOs, in the order given
   */
  List<UserDTO> mapToListDTO(Collection<User> entities);

  /**
   * Maps each given user DTO into a user entity, collected into a set. Each entity leaves
   * externalId unset, leaves groups empty and keeps status at the value its constructor assigns;
   * two dtos with the same sub and issuer collapse into one entity.
   *
   * @param dtos dtos to map
   * @return the mapped entities, one per distinct sub and issuer
   */
  Set<User> mapToSet(Collection<UserDTO> dtos);

  /**
   * Maps each given user entity into a user DTO, collected into a set. Each DTO's roles stays the
   * empty set it is declared with.
   *
   * @param entities entities to map
   * @return the mapped DTOs
   */
  Set<UserDTO> mapToSetDTO(Collection<User> entities);

  /**
   * Maps a user DTO into a user entity, wrapped in an Optional. Copies id, sub, iss, fullName,
   * mail, givenName, familyName and picture; externalId stays unset, groups stays empty, and status
   * keeps the value the entity's no-argument constructor assigns.
   *
   * @param dto dto to map
   * @return the mapped entity, or an empty Optional when the dto is null
   */
  default Optional<User> mapToOptional(UserDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  /**
   * Maps a user entity into a user DTO, wrapped in an Optional. Copies id, sub, iss, fullName,
   * mail, givenName, familyName and picture; roles stays the empty set it is declared with.
   *
   * @param entity entity to map
   * @return the mapped DTO, or an empty Optional when the entity is null
   */
  default Optional<UserDTO> mapToOptional(User entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  /**
   * Maps the content of the given page into user DTOs, each with roles left as the empty set it is
   * declared with. The returned page's element count and totals reflect only the mapped content of
   * this page, not the totals of the original page.
   *
   * @param objects page to map
   * @return a page carrying the mapped content, with pagination limited to this page's own size
   */
  default Page<UserDTO> mapToPageDTO(Page<User> objects) {
    List<UserDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps the content of the given page into user entities, each leaving externalId unset, groups
   * empty and status at the value its constructor assigns. The returned page's element count and
   * totals reflect only the mapped content of this page, not the totals of the original page.
   *
   * @param objects page to map
   * @return a page carrying the mapped content, with pagination limited to this page's own size
   */
  default Page<User> mapToPage(Page<UserDTO> objects) {
    List<User> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Wraps the given page of users into a page result, with each user's roles filled in from every
   * group the user belongs to and each role carrying its microservice id and name. The pagination
   * carries the original page's true totals.
   *
   * @param objects page to wrap
   * @return the page result carrying the users with their roles, and the page's pagination
   */
  default PageResultResource<UserDTO> mapToPageResultResource(Page<User> objects) {
    List<UserDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToUserDTOWithRoles(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  /**
   * Wraps the given page of users into a page result of basic views. The pagination carries the
   * original page's true totals.
   *
   * @param users page to wrap
   * @return the page result carrying the basic views, and the page's pagination
   */
  default PageResultResource<UserBasicViewDto> mapToPageUserBasicViewDto(Page<User> users) {
    List<UserBasicViewDto> mapped = new ArrayList<>();
    users.forEach(user -> mapped.add(mapToBasicViewDto(user)));
    return new PageResultResource<>(mapped, createPagination(users));
  }

  /**
   * Wraps the given page of users into a page result of basic views, replacing every user's name,
   * sub and mail with a placeholder except for the user matching the given id. The pagination
   * carries the original page's true totals.
   *
   * @param users page to wrap
   * @param loggedInUserId id of the user whose own details stay unmasked
   * @return the page result carrying the basic views, and the page's pagination
   */
  default PageResultResource<UserBasicViewDto> mapToPageUserBasicViewDTOAnonymize(
      Page<User> users, Long loggedInUserId) {
    List<UserBasicViewDto> mapped = new ArrayList<>();
    users.forEach(user -> mapped.add(mapToBasicViewDtoAnonymize(user, loggedInUserId)));
    return new PageResultResource<>(mapped, createPagination(users));
  }

  /**
   * Builds a basic view of the given user, replacing the full name, sub and mail with a fixed
   * placeholder and the given name and family name with shorter placeholders, unless the given id
   * matches the user's own id. The id, issuer and picture are never replaced; the picture becomes a
   * defensive copy of the user's picture, or stays unset when the user has none.
   *
   * @param user user to view
   * @param loggedInUserId id compared against the user's own id
   * @return the basic view, or null when the user is null
   */
  default UserBasicViewDto mapToBasicViewDtoAnonymize(User user, Long loggedInUserId) {
    UserBasicViewDto userBasicViewDto = mapToBasicViewDto(user);
    if (userBasicViewDto != null && !Objects.equals(loggedInUserId, user.getId())) {
      userBasicViewDto.setFullName("other player");
      userBasicViewDto.setSub("other player");
      userBasicViewDto.setMail("other player");
      userBasicViewDto.setGivenName("other");
      userBasicViewDto.setFamilyName("player");
    }
    return userBasicViewDto;
  }

  /**
   * Wraps the given page of users into a page result of group-listing views. The pagination carries
   * the original page's true totals.
   *
   * @param objects page to wrap
   * @return the page result carrying the group-listing views, and the page's pagination
   */
  default PageResultResource<UserForGroupsDTO> mapToPageResultResourceForGroups(
      Page<User> objects) {
    List<UserForGroupsDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapEntityToUserForGroupsDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  /**
   * Maps the given user into a user DTO with its roles filled in from every group the user belongs
   * to; each role carries its microservice id and name.
   *
   * @param user user to map
   * @return the user's data together with its aggregated roles
   */
  @Named("mapToUserDTOWithRoles")
  default UserDTO mapToUserDTOWithRoles(User user) {
    UserDTO userDTO = mapToDTO(user);
    userDTO.setPicture(user.getPicture());
    Set<Role> rolesOfUser = new HashSet<>();
    for (IDMGroup groupOfUser : user.getGroups()) {
      rolesOfUser.addAll(groupOfUser.getRoles());
    }
    userDTO.setRoles(rolesOfUser.stream().map(this::convertToRoleDTO).collect(Collectors.toSet()));
    return userDTO;
  }

  private RoleDTO convertToRoleDTO(Role role) {
    RoleDTO roleDTO = mapRoleToDTO(role);
    roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
    roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
    return roleDTO;
  }
}
