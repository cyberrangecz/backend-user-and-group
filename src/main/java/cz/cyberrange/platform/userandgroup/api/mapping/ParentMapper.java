package cz.cyberrange.platform.userandgroup.api.mapping;

import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import org.springframework.data.domain.Page;

/** Base for the mappers, giving each one a shared way to build the API's pagination metadata. */
public interface ParentMapper {

  /**
   * Builds the pagination metadata describing the given page: its page number, element count, size,
   * total elements and total pages.
   *
   * @param objects page to describe
   * @return pagination metadata mirroring the given page
   */
  default PageResultResource.Pagination createPagination(Page<?> objects) {
    PageResultResource.Pagination pageMetadata = new PageResultResource.Pagination();
    pageMetadata.setNumber(objects.getNumber());
    pageMetadata.setNumberOfElements(objects.getNumberOfElements());
    pageMetadata.setSize(objects.getSize());
    pageMetadata.setTotalElements(objects.getTotalElements());
    pageMetadata.setTotalPages(objects.getTotalPages());
    return pageMetadata;
  }
}
