package cz.muni.ics.kypo.userandgroup.persistence.domain;

import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MicroserviceTest {

    @Autowired
    private TestEntityManager entityManager;

    private final String name = "Training";
    private final String endpoint = "/training/roles";

    @Test
    public void createWhenNameIsNullShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Microservice(null, endpoint));
        assertEquals("Name of microservice must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenNameIsEmptyShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Microservice("", endpoint));
        assertEquals("Name of microservice must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenEndpointIsNullShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Microservice(name, null));
        assertEquals("Endpoint of microservice must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenEndpointIsEmptyShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Microservice(name, ""));
        assertEquals("Endpoint of microservice must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenEndpoinContainsWhitespaceShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new Microservice(name, "/training / roles"));
        assertEquals("Endpoint of microservice must not contain whitespace", ex.getMessage());
    }

    @Test
    public void saveShouldPersistData() {
        Microservice m = this.entityManager.persistFlushFind(new Microservice(name, endpoint));
        assertEquals(name, m.getName());
        assertEquals(endpoint, m.getEndpoint());
    }
}
