package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
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
     * @throws EntityNotFoundException if microservice was not found.
     */
    Microservice getMicroserviceById(Long id);

    /**
     * Gets microservice with the given name from the database.
     *
     * @param microserviceName name of the microservice to be loaded.
     * @return the microservice with the given name.
     * @throws EntityNotFoundException if microservice was not found.
     */
    Microservice getMicroserviceByName(String microserviceName);

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
    Microservice createMicroservice(Microservice microservice);

    /**
     * Check if microservice with the given name is in database.
     *
     * @param microserviceName name of the microservice to check.
     * @return true if the {@link Microservice} exists in DB, false otherwise.
     */
    boolean existsByName(String microserviceName);
}
