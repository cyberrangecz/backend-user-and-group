package cz.muni.ics.kypo.userandgroup.api.facade;

import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;

/**
 * The interface for the Microservice facade layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface MicroserviceFacade {

    /**
     *  Register new microservice in main microservice <i>User-and-group</i>.
     *
     * @param newMicroserviceDTO information about new microservice to be created {@link NewMicroserviceDTO}.
     * @throws UserAndGroupFacadeException if the microservice with given name or endpoint already exist.
     */
    void registerNewMicroservice(NewMicroserviceDTO newMicroserviceDTO);
}
