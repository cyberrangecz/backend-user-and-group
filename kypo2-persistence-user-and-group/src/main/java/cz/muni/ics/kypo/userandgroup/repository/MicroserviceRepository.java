package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

/**
 * The JPA repository interface to manage {@link Microservice} instances.
 */
public interface MicroserviceRepository extends JpaRepository<Microservice, Long>, QuerydslPredicateExecutor<Microservice> {
    
    /**
     * Find Microservice by its name.
     *
     * @param name the name of the looking Microservice
     * @return the {@link Microservice} if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<Microservice> findByName(String name);

    /**
     * Check if microservice exist.
     *
     * @param name the name of the looking Microservice
     * @return true if microservice with given name exists in DB, false otherwise.
     */
    boolean existsByName(String name);
}
