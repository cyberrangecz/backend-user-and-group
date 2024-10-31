package cz.muni.ics.kypo.userandgroup.mapping;

import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The UserMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type User and
 * DTOs classes. Code is generated during compile time.
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends ParentMapper {
    User mapToEntity(UserDTO dto);

    User mapToEntity(UserCreateDTO dto);

    User mapToEntity(UserImportDTO dto);

    User mapToEntity(UserUpdateDTO dto);

    UserUpdateDTO mapUserDTOToUserUpdateDTO(UserDTO dto);

    RoleDTO mapRoleToDTO(Role entity);

    UserDTO mapToDTO(User entity);

    UserBasicViewDto mapToBasicViewDto(User entity);

    User mapUserForGroupsDTOToEntity(UserForGroupsDTO userForGroupsDTO);

    UserForGroupsDTO mapEntityToUserForGroupsDTO(User user);

    List<User> mapToList(Collection<UserDTO> dtos);

    Set<User> mapUsersImportToSet(Collection<UserImportDTO> dtos);

    List<UserDTO> mapToListDTO(Collection<User> entities);

    Set<User> mapToSet(Collection<UserDTO> dtos);

    Set<UserDTO> mapToSetDTO(Collection<User> entities);

    default Optional<User> mapToOptional(UserDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<UserDTO> mapToOptional(User entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<UserDTO> mapToPageDTO(Page<User> objects) {
        List<UserDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<User> mapToPage(Page<UserDTO> objects) {
        List<User> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<UserDTO> mapToPageResultResource(Page<User> objects) {
        List<UserDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToUserDTOWithRoles(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }

    default PageResultResource<UserBasicViewDto> mapToPageUserBasicViewDto(Page<User> users) {
        List<UserBasicViewDto> mapped = new ArrayList<>();
        users.forEach(user -> mapped.add(mapToBasicViewDto(user)));
        return new PageResultResource<>(mapped, createPagination(users));
    }

    default PageResultResource<UserBasicViewDto> mapToPageUserBasicViewDTOAnonymize(Page<User> users, Long loggedInUserId) {
        List<UserBasicViewDto> mapped = new ArrayList<>();
        users.forEach(user -> mapped.add(mapToBasicViewDtoAnonymize(user, loggedInUserId)));
        return new PageResultResource<>(mapped, createPagination(users));
    }

    default UserBasicViewDto mapToBasicViewDtoAnonymize(User user, Long loggedInUserId) {
        if ( user == null ) {
            return null;
        }

        UserBasicViewDto userBasicViewDto = new UserBasicViewDto();

        userBasicViewDto.setId( user.getId() );
        userBasicViewDto.setFullName( loggedInUserId != user.getId() ? "other player" : user.getFullName() );
        userBasicViewDto.setSub( loggedInUserId != user.getId() ? "other player" : user.getSub() );
        userBasicViewDto.setMail( loggedInUserId != user.getId() ? "other player" : user.getMail() );
        userBasicViewDto.setGivenName( loggedInUserId != user.getId() ? "other" : user.getGivenName() );
        userBasicViewDto.setFamilyName( loggedInUserId != user.getId() ? "player" : user.getFamilyName() );
        userBasicViewDto.setIss( user.getIss() );
        byte[] picture = user.getPicture();
        if ( picture != null ) {
            userBasicViewDto.setPicture( Arrays.copyOf( picture, picture.length ) );
        }

        return userBasicViewDto;
    }

    default PageResultResource<UserForGroupsDTO> mapToPageResultResourceForGroups(Page<User> objects) {
        List<UserForGroupsDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapEntityToUserForGroupsDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }


    @Named("mapToUserDTOWithRoles")
    default UserDTO mapToUserDTOWithRoles(User user) {
        UserDTO userDTO = mapToDTO(user);
        userDTO.setPicture(user.getPicture());
        Set<Role> rolesOfUser = new HashSet<>();
        for (IDMGroup groupOfUser : user.getGroups()) {
            rolesOfUser.addAll(groupOfUser.getRoles());
        }
        userDTO.setRoles(rolesOfUser.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toSet()));
        return userDTO;
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = mapRoleToDTO(role);
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        return roleDTO;
    }
}
