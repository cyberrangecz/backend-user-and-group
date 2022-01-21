package cz.muni.ics.kypo.userandgroup.persistence.repository;

import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class MicroserviceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MicroserviceRepository microserviceRepository;

    private final String name = "training";
    private final String endpoint = "/training";

    @Test
    public void findByName() throws Exception {
        Microservice microservice = new Microservice(name, endpoint);
        this.entityManager.persist(microservice);
        Optional<Microservice> optionalMicroservice = this.microserviceRepository.findByName(name);
        Microservice m = optionalMicroservice.orElseThrow(() -> new Exception("Microservice shoul be found"));
        assertEquals(microservice, m);
        assertEquals(microservice.getName(), m.getName());
        assertEquals(microservice.getEndpoint(), m.getEndpoint());
    }

    @Test
    public void findByNameNotFound() {
        assertFalse(this.microserviceRepository.findByName("not existing microservice").isPresent());
    }
}
