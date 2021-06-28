package cz.muni.ics.kypo.userandgroup.mapping.mapstruct;

import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The RoleMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type Role and
 * DTOs classes. Code is generated during compile time.
 */
@Mapper(componentModel = "spring", uses = {MicroserviceMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper extends ParentMapper {

    Role mapToEntity(RoleDTO dto);

    Role mapToEntity(RoleForNewMicroserviceDTO dto);

    RoleDTO mapToDTO(Role entity);

    List<Role> mapToList(Collection<RoleDTO> dtos);

    List<RoleDTO> mapToListDTO(Collection<Role> entities);

    Set<Role> mapToSet(Collection<RoleDTO> dtos);

    Set<Role> mapToSetOfNewRoles(Collection<RoleForNewMicroserviceDTO> dtos);

    @IterableMapping(qualifiedByName = "roleToRoleDTOWithMicroservice")
    Set<RoleDTO> mapToSetDTO(Collection<Role> entities);

    @Named("roleToRoleDTOWithMicroservice")
    default RoleDTO mapToRoleDTOWithMicroservice(Role entity) {
        RoleDTO roleDTO = mapToDTO(entity);
        roleDTO.setIdOfMicroservice(entity.getMicroservice().getId());
        roleDTO.setNameOfMicroservice(entity.getMicroservice().getName());
        return roleDTO;
    }

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
