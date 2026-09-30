package cz.cyberrange.platform.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Business logic for creating and querying microservices. */
@Service
public class MicroserviceService {

  private final MicroserviceRepository microserviceRepository;

  @Autowired
  public MicroserviceService(MicroserviceRepository microserviceRepository) {
    this.microserviceRepository = microserviceRepository;
  }

  /**
   * Returns the microservice with the given id.
   *
   * @param id id of the microservice
   * @return the matching microservice
   * @throws EntityNotFoundException when no microservice has that id
   */
  public Microservice getMicroserviceById(Long id) {
    return microserviceRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(Microservice.class, "id", id.getClass(), id)));
  }

  /**
   * Returns the microservice with the given name.
   *
   * @param microserviceName name of the microservice
   * @return the matching microservice
   * @throws EntityNotFoundException when no microservice has that name
   */
  public Microservice getMicroserviceByName(String microserviceName) {
    return microserviceRepository
        .findByName(microserviceName)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        Microservice.class,
                        "microserviceName",
                        microserviceName.getClass(),
                        microserviceName)));
  }

  /**
   * Returns every microservice that matches the given predicate.
   *
   * @param predicate filter applied to the microservices
   * @param pageable page and sort request
   * @return the matching page of microservices; empty page when none match
   */
  public Page<Microservice> getMicroservices(Predicate predicate, Pageable pageable) {
    return microserviceRepository.findAll(predicate, pageable);
  }

  /**
   * Persists the given microservice.
   *
   * @param microserviceToCreate microservice to create
   * @return the persisted microservice
   */
  public Microservice createMicroservice(Microservice microserviceToCreate) {
    return microserviceRepository.save(microserviceToCreate);
  }

  /**
   * Checks whether a microservice with the given name exists.
   *
   * @param microserviceName name to match
   * @return true when a microservice has that name, false otherwise
   */
  public boolean existsByName(String microserviceName) {
    return microserviceRepository.existsByName(microserviceName);
  }
}
