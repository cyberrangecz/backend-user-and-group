package cz.muni.ics.kypo.userandgroup.mapping.mapstruct;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import org.springframework.data.domain.Page;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface ParentMapper {

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
