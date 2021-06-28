package cz.muni.ics.kypo.userandgroup.entities;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class MicroserviceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String name = "Training";
    private String endpoint = "/training/roles";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenNameIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of microservice must not be empty");
        new Microservice(null, endpoint);
    }

    @Test
    public void createWhenNameIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of microservice must not be empty");
        new Microservice("", endpoint);
    }

    @Test
    public void createWhenEndpointIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Endpoint of microservice must not be empty");
        new Microservice(name, null);
    }

    @Test
    public void createWhenEndpointIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Endpoint of microservice must not be empty");
        new Microservice(name, "");
    }

    @Test
    public void createWhenEndpoinContainsWhitespaceShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Endpoint of microservice must not contain whitespace");
        new Microservice(name, "/training / roles");
    }

    @Test
    public void saveShouldPersistData() {
        Microservice m = this.entityManager.persistFlushFind(new Microservice(name, endpoint));
        assertEquals(name, m.getName());
        assertEquals(endpoint, m.getEndpoint());
    }
}
