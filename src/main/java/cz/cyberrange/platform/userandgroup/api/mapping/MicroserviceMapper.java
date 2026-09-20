package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Converts between {@link Microservice} entities and microservice DTOs, in both directions and
 * across collections and pages.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MicroserviceMapper extends ParentMapper {

  /**
   * Maps a new microservice registration to a microservice entity. The id is left unset; the roles
   * carried by the registration have no matching field and are not carried over.
   *
   * @param dto microservice registration to map
   * @return the mapped microservice
   */
  Microservice mapCreateToEntity(NewMicroserviceDTO dto);

  /**
   * Maps a microservice entity to a microservice DTO.
   *
   * @param entity microservice to map
   * @return the mapped microservice
   */
  MicroserviceDTO mapToDTO(Microservice entity);

  /**
   * Maps a microservice DTO to a microservice entity.
   *
   * @param dto microservice to map
   * @return the mapped microservice
   */
  Microservice mapToEntity(MicroserviceDTO dto);

  /**
   * Maps each microservice DTO in the given collection to a microservice entity.
   *
   * @param dtos microservices to map
   * @return the mapped microservices, in a new list
   */
  List<Microservice> mapToList(Collection<MicroserviceDTO> dtos);

  /**
   * Maps each microservice entity in the given collection to a microservice DTO.
   *
   * @param entities microservices to map
   * @return the mapped microservices, in a new list
   */
  List<MicroserviceDTO> mapToListDTO(Collection<Microservice> entities);

  /**
   * Maps each microservice DTO in the given collection to a microservice entity.
   *
   * @param dtos microservices to map
   * @return the mapped microservices, in a new set
   */
  Set<Microservice> mapToSet(Collection<MicroserviceDTO> dtos);

  /**
   * Maps each microservice entity in the given collection to a microservice DTO.
   *
   * @param entities microservices to map
   * @return the mapped microservices, in a new set
   */
  Set<MicroserviceDTO> mapToSetDTO(Collection<Microservice> entities);

  /**
   * Maps a microservice DTO to a microservice entity.
   *
   * @param dto microservice to map
   * @return the mapped microservice, empty when dto is null
   */
  default Optional<Microservice> mapToOptional(MicroserviceDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  /**
   * Maps a microservice entity to a microservice DTO.
   *
   * @param entity microservice to map
   * @return the mapped microservice, empty when entity is null
   */
  default Optional<MicroserviceDTO> mapToOptional(Microservice entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  /**
   * Maps a page of microservice entities to a page of microservice DTOs, keeping the given page's
   * pageable. The total element count of the returned page is the number of mapped microservices,
   * not the original page's total.
   *
   * @param objects page of microservices to map
   * @return the mapped page
   */
  default Page<MicroserviceDTO> mapToPageDTO(Page<Microservice> objects) {
    List<MicroserviceDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of microservice DTOs to a page of microservice entities, keeping the given page's
   * pageable. The total element count of the returned page is the number of mapped microservices,
   * not the original page's total.
   *
   * @param objects page of microservice DTOs to map
   * @return the mapped page
   */
  default Page<Microservice> mapToPage(Page<MicroserviceDTO> objects) {
    List<Microservice> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  /**
   * Maps a page of microservice entities to a paginated result carrying microservice DTOs. The
   * pagination metadata reflects the original page.
   *
   * @param objects page of microservices to map
   * @return the mapped microservices together with the page's pagination metadata
   */
  default PageResultResource<MicroserviceDTO> mapToPageResultResource(Page<Microservice> objects) {
    List<MicroserviceDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
