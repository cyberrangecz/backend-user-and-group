package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.google.common.base.Preconditions;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * @author Dominik Pilar
 */
@Api(value = "Endpoint for Microservices", hidden = true)
@ApiIgnore
@RestController
@RequestMapping(path = "/microservices")
public class MicroserviceRestController {

    private static Logger LOG = LoggerFactory.getLogger(RoleRestController.class);

    private MicroserviceFacade microserviceFacade;

    @Autowired
    public MicroserviceRestController(MicroserviceFacade microserviceFacade) {
        this.microserviceFacade = microserviceFacade;
    }

    @ApiOperation(httpMethod = "POST",
            value = "Register new microservice.",
            response = Void.class,
            nickname = "registerNewMicroservice",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Giver microservice registered and created."),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerNewMicroservice(@ApiParam(value = "Microservice to be created with roles.", required = true)
                                                        @Valid @RequestBody NewMicroserviceDTO microserviceDTO) {
        LOG.debug("registerNewMicroservice({})", microserviceDTO);
        Preconditions.checkNotNull(microserviceDTO);
        try {
            microserviceFacade.registerNewMicroservice(microserviceDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotCreatedException("Microservice cannot be created in database: " + e.getMessage());
        }
    }
}
