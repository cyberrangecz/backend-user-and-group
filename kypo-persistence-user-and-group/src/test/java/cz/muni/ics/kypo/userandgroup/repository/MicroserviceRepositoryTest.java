package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.entities"})
public class MicroserviceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MicroserviceRepository microserviceRepository;

    private String name = "training";
    private String endpoint = "/training";

    @SpringBootApplication
    static class TestConfiguration {
    }

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
