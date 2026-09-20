package cz.cyberrange.platform.userandgroup.rest.controller;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.rest.facade.MicroserviceFacade;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for registering microservices, along with the roles each one defines, and for querying
 * microservices already registered.
 */
@Hidden
@Tag(
    name = "microservices",
    description = "Services registered with the platform and the roles each one defines.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "401",
          description = "Missing or invalid bearer token.",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(
          responseCode = "500",
          description = "Unexpected server error.",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
@RestController
@RequestMapping(path = "/microservices")
public class MicroservicesRestController {

  private final MicroserviceFacade microserviceFacade;

  @Autowired
  public MicroservicesRestController(MicroserviceFacade microserviceFacade) {
    this.microserviceFacade = microserviceFacade;
  }

  /**
   * Returns every microservice that matches the given predicate. Requires the administrator
   * authority; answers with HTTP 401 when the caller is not authenticated.
   *
   * @param predicate optional filter on microservice attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @return HTTP 200 with the matching page of microservices; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getMicroservices",
      summary = "List registered microservices",
      description =
          "Text filters match any part of a value and ignore case. Every other filter must match"
              + " the whole value.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The matching page of microservices, empty when nothing matches."),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<MicroserviceDTO>> getMicroservices(
      @QuerydslPredicate(root = Microservice.class) Predicate predicate,
      @ParameterObject Pageable pageable) {
    PageResultResource<MicroserviceDTO> microserviceDTos =
        microserviceFacade.getAllMicroservices(predicate, pageable);
    return ResponseEntity.ok(microserviceDTos);
  }

  /**
   * Registers the given microservice together with its roles. When a microservice with that name
   * already exists, its endpoint is updated and its roles are reconciled instead of creating a new
   * microservice: each given role not already defined for it is added, and a role flagged as
   * default replaces the microservice's previous default role in the group that holds every
   * microservice's default role. For a microservice registered for the first time, each given role
   * is added directly, and a role flagged as default is added to that same group. No authentication
   * or authority is required to call this method. Answers with HTTP 400 when the request body fails
   * validation.
   *
   * @param microserviceDTO microservice to register, together with its roles
   * @return HTTP 201 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException
   *     (HTTP 422) when more than one of the given roles is flagged as default
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when a role with the same role type as one of the given roles already exists
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when an existing microservice being given a new default role has no default role of
   *     its own to replace
   */
  @Operation(
      operationId = "registerNewMicroservice",
      summary = "Register a microservice and its roles",
      description =
          "This operation needs no bearer token. A name already registered gets its endpoint"
              + " updated and its new roles added. At most one role may be the default one.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "The microservice was registered."),
        @ApiResponse(
            responseCode = "400",
            description = "The request body is not valid.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "The microservice has no default role for the new one to replace.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "A role with the same role type already exists.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "422",
            description = "More than one role is marked as the default one.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> registerNewMicroservice(
      @Valid @RequestBody NewMicroserviceDTO microserviceDTO) {
    microserviceFacade.registerMicroservice(microserviceDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
