package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupWithRolesDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The IDMGroupMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type IDMGroup and
 * DTOs classes. Code is generated during compile time.
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IDMGroupMapper extends ParentMapper {

    IDMGroup mapToEntity(GroupDTO dto);

    GroupDTO mapToDTO(IDMGroup entity);

    GroupViewDTO mapToViewDTO(IDMGroup entity);

    GroupWithRolesDTO mapToWithRolesDto(IDMGroup entity);

    IDMGroup mapCreateToEntity(NewGroupDTO dto);

    IDMGroup mapUpdateToEntity(UpdateGroupDTO dto);

    List<IDMGroup> mapToList(Collection<GroupDTO> dtos);

    List<GroupViewDTO> mapToListDTO(Collection<IDMGroup> entities);

    Set<IDMGroup> mapToSet(Collection<GroupDTO> dtos);

    Set<GroupDTO> mapToSetDTO(Collection<IDMGroup> entities);

    default Optional<IDMGroup> mapToOptional(GroupDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<GroupDTO> mapToOptional(IDMGroup entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<GroupViewDTO> mapToPageDTO(Page<IDMGroup> objects) {
        List<GroupViewDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<GroupViewDTO> mapToPageResultResource(Page<IDMGroup> objects) {
        List<GroupViewDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToViewDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
