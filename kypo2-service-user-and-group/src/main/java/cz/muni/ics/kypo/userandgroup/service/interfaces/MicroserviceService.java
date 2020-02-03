package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The interface for the Microservice service layer.
 */
public interface MicroserviceService {

    /**
     * Gets microservice with the given ID from the database.
     *
     * @param id ID of the microservice to be loaded.
     * @return the microservice with the given ID.
     * @throws UserAndGroupServiceException if microservice was not found.
     */
    Microservice getMicroserviceById(Long id);

    /**
     * Returns all microservices from the database (paginated).
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of {@link Microservice}s.
     */
    Page<Microservice> getMicroservices(Predicate predicate, Pageable pageable);

    /**
     * Create a given microservice in database.
     *
     * @param microservice to be created.
     * @return true if the {@link Microservice} is created, false if {@link Microservice} already exists in DB.
     */
    boolean createMicroservice(Microservice microservice);
}
