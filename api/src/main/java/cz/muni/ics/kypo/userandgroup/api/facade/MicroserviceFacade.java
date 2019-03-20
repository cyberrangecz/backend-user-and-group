package cz.muni.ics.kypo.userandgroup.api.facade;

import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;

public interface MicroserviceFacade {

    /**
     *  Register new microservice in user and group with its.
     *
     * @param newMicroserviceDTO microservice to be created
     * @throws UserAndGroupFacadeException if microservice with given name or endpoint already exist.
     */
    void registerNewMicroservice(NewMicroserviceDTO newMicroserviceDTO);
}
