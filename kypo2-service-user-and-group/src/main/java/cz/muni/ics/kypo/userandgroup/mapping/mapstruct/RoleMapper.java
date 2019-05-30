package cz.muni.ics.kypo.userandgroup.mapping.mapstruct;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.model.Role;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Mapper(componentModel = "spring", uses = {MicroserviceMapper.class})
public interface RoleMapper extends ParentMapper {

    Role mapToEntity(RoleDTO dto);

    RoleDTO mapToDTO(Role entity);

    List<Role> mapToList(Collection<RoleDTO> dtos);

    List<RoleDTO> mapToListDTO(Collection<Role> entities);

    Set<Role> mapToSet(Collection<RoleDTO> dtos);

    Set<RoleDTO> mapToSetDTO(Collection<Role> entities);

    default Optional<Role> mapToOptional(RoleDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<RoleDTO> mapToOptional(Role entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<RoleDTO> mapToPageDTO(Page<Role> objects) {
        List<RoleDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<Role> mapToPage(Page<RoleDTO> objects) {
        List<Role> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<RoleDTO> mapToPageResultResource(Page<Role> objects) {
        List<RoleDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
