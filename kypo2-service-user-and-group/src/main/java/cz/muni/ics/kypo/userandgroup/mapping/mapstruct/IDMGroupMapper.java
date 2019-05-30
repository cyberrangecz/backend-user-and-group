package cz.muni.ics.kypo.userandgroup.mapping.mapstruct;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface IDMGroupMapper extends ParentMapper {

    IDMGroup mapToEntity(GroupDTO dto);

    GroupDTO mapToDTO(IDMGroup entity);

    IDMGroup mapCreateToEntity(NewGroupDTO dto);

    IDMGroup mapUpdateToEntity(UpdateGroupDTO dto);

    List<IDMGroup> mapToList(Collection<GroupDTO> dtos);

    List<GroupDTO> mapToListDTO(Collection<IDMGroup> entities);

    Set<IDMGroup> mapToSet(Collection<GroupDTO> dtos);

    Set<GroupDTO> mapToSetDTO(Collection<IDMGroup> entities);

    default Optional<IDMGroup> mapToOptional(GroupDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<GroupDTO> mapToOptional(IDMGroup entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<GroupDTO> mapToPageDTO(Page<IDMGroup> objects) {
        List<GroupDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<IDMGroup> mapToPage(Page<GroupDTO> objects) {
        List<IDMGroup> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<GroupDTO> mapToPageResultResource(Page<IDMGroup> objects) {
        List<GroupDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
