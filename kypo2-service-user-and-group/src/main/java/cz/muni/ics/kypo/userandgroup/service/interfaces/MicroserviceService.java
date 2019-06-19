package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;

import java.util.List;

/**
 * The interface for the Microservice service layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface MicroserviceService {

    /**
     * Gets microservice with the given ID from the database.
     *
     * @param id ID of the microservice to be loaded.
     * @return the microservice with the given ID.
     * @throws UserAndGroupServiceException if microservice was not found.
     */
    Microservice get(Long id);

    /**
     * Returns list of all microservices from the database.
     *
     * @return list of {@link Microservice}s.
     */
    List<Microservice> getMicroservices();

    /**
     * Create a given microservice in database.
     *
     * @param microservice to be created.
     * @return created {@link Microservice}.
     */
    Microservice create(Microservice microservice);
}
