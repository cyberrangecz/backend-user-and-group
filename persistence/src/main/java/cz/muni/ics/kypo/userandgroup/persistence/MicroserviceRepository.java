package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.Microservice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MicroserviceRepository extends JpaRepository<Microservice, Long> {

    Optional<Microservice> findByName(String name);
}
