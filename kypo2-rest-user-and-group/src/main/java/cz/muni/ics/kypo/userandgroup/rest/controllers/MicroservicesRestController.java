package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.google.common.base.Preconditions;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
import cz.muni.ics.kypo.userandgroup.rest.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Rest controller for the Microservice resource.
 *
 * @author Dominik Pilar
 */
@Api(value = "Endpoint for Microservices", hidden = true)
@RestController
@RequestMapping(path = "/microservices")
public class MicroservicesRestController {

    private static Logger LOG = LoggerFactory.getLogger(RolesRestController.class);

    private MicroserviceFacade microserviceFacade;

    /**
     * Instantiates a new MicroservicesRestController.
     *
     * @param microserviceFacade the microservice facade
     */
    @Autowired
    public MicroservicesRestController(MicroserviceFacade microserviceFacade) {
        this.microserviceFacade = microserviceFacade;
    }

    /**
     * Register new microservice in main microservice User-and-group.
     *
     * @param microserviceDTO the microservice to be registered.
     * @return the response entity with specific status code and header.
     */
    @ApiOperation(httpMethod = "POST",
            value = "Register new microservice.",
            nickname = "registerNewMicroservice",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Microservice registered and created."),
            @ApiResponse(code = 406, message = "Microservice not created and registered because of some specific reason."),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)

    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerNewMicroseervice(@ApiParam(value = "Microservice to be created with roles.", required = true)
                                                        @Valid @RequestBody NewMicroserviceDTO microserviceDTO) {
        Preconditions.checkNotNull(microserviceDTO);
        try {
            microserviceFacade.registerNewMicroservice(microserviceDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotCreatedException("Microservice cannot be created in database: " + e.getMessage());
        }
    }
}
