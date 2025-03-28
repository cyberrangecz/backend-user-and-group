package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
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
 * The MicroserviceMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type Microservice and
 * DTOs classes. Code is generated during compile time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MicroserviceMapper extends ParentMapper {

    Microservice mapCreateToEntity(NewMicroserviceDTO dto);

    MicroserviceDTO mapToDTO(Microservice entity);

    Microservice mapToEntity(MicroserviceDTO dto);

    List<Microservice> mapToList(Collection<MicroserviceDTO> dtos);

    List<MicroserviceDTO> mapToListDTO(Collection<Microservice> entities);

    Set<Microservice> mapToSet(Collection<MicroserviceDTO> dtos);

    Set<MicroserviceDTO> mapToSetDTO(Collection<Microservice> entities);

    default Optional<Microservice> mapToOptional(MicroserviceDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<MicroserviceDTO> mapToOptional(Microservice entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<MicroserviceDTO> mapToPageDTO(Page<Microservice> objects) {
        List<MicroserviceDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<Microservice> mapToPage(Page<MicroserviceDTO> objects) {
        List<Microservice> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<MicroserviceDTO> mapToPageResultResource(Page<Microservice> objects) {
        List<MicroserviceDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
