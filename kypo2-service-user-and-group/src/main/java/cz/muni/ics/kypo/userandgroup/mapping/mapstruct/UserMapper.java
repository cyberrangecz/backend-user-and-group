package cz.muni.ics.kypo.userandgroup.mapping.mapstruct;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.model.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper extends ParentMapper {
    User mapToEntity(UserDTO dto);

    UserDTO mapToDTO(User entity);

    User mapUserForGroupsDTOToEntity(UserForGroupsDTO userForGroupsDTO);

    UserForGroupsDTO mapEntityToUserForGroupsDTO(User user);

    List<User> mapToList(Collection<UserDTO> dtos);

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
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }

    default PageResultResource<UserForGroupsDTO> mapToPageResultResourceForGroups(Page<User> objects) {
        List<UserForGroupsDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapEntityToUserForGroupsDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
