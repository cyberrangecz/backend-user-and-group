package cz.cyberrange.platform.userandgroup.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.annotations.swagger.ApiPageableSwagger;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.rest.facade.MicroserviceFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Rest controller for the Microservice resource.
 */
@Api(value = "Endpoint for Microservices",
        hidden = true, tags = "microservices",
        authorizations = @Authorization(value = "bearerAuth"))
@RestController
@RequestMapping(path = "/microservices")
public class MicroservicesRestController {

    private final MicroserviceFacade microserviceFacade;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new MicroservicesRestController.
     *
     * @param microserviceFacade the microservice facade
     * @param objectMapper       the object mapper
     */
    @Autowired
    public MicroservicesRestController(MicroserviceFacade microserviceFacade, ObjectMapper objectMapper) {
        this.microserviceFacade = microserviceFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets microservices.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @return the microservices
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all microservices.",
            nickname = "getMicroservices",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Microservice not found.", response = MicroservicesRestController.MicroserviceRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMicroservices(@ApiParam(value = "Filtering on Microservice entity attributes", required = false)
                                                   @QuerydslPredicate(root = Microservice.class) Predicate predicate,
                                                   Pageable pageable,
                                                   @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                   @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<MicroserviceDTO> microserviceDTos = microserviceFacade.getAllMicroservices(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, microserviceDTos));
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
    public ResponseEntity<Void> registerNewMicroservice(@ApiParam(value = "Microservice to be created with roles.", required = true)
                                                        @Valid @RequestBody NewMicroserviceDTO microserviceDTO) {
        microserviceFacade.registerMicroservice(microserviceDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiModel(value = "MicroserviceRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages.")
    public static class MicroserviceRestResource extends PageResultResource<MicroserviceDTO> {
        @ApiModelProperty(value = "Retrieved microservices from databases.", required = true)
        private List<MicroserviceDTO> content;
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.", required = true)
        private Pagination pagination;
    }

}
