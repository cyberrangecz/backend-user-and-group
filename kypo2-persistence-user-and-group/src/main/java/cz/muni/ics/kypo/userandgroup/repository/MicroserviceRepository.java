package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Microservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface MicroserviceRepository extends JpaRepository<Microservice, Long>,
        QuerydslPredicateExecutor<Microservice> {

    Optional<Microservice> findByName(String name);
}
