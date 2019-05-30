package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;

import java.util.List;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface MicroserviceService {

    /**
     * Gets microservice with given id from database.
     *
     * @param id of the microservice to be loaded
     * @return microservice with given id
     * @throws UserAndGroupServiceException if microservice was not found
     */
    Microservice get(Long id);

    /**
     * Returns all Microservices from database.
     *
     * @return Microservices in database
     */
    List<Microservice> getMicroservices();

    /**
     * Create new microservice.
     *
     * @param microservice to be created
     * @return Created microservice
     */
    Microservice create(Microservice microservice);
}
