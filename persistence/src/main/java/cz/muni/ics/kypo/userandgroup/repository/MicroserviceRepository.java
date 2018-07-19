package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Microservice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MicroserviceRepository extends JpaRepository<Microservice, Long> {

    Optional<Microservice> findByName(String name);
}
