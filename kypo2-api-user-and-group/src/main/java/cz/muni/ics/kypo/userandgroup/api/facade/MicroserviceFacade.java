package cz.muni.ics.kypo.userandgroup.api.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;

/**
 * The interface for the Microservice facade layer.
 */
public interface MicroserviceFacade {

    /**
     * Returns all microservices from the database (paginated).
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return paginated microservices
     */
    PageResultResource<MicroserviceDTO> getAllMicroservices(Predicate predicate, Pageable pageable);

    /**
     * Register new microservice in main microservice <i>User-and-group</i>.
     *
     * @param newMicroserviceDTO information about new microservice to be created {@link NewMicroserviceDTO}.
     * @throws UserAndGroupFacadeException if the microservice with given name or endpoint already exist.
     */
    void registerMicroservice(NewMicroserviceDTO newMicroserviceDTO);
}
