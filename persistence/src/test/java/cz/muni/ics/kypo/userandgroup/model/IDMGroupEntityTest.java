package cz.muni.ics.kypo.userandgroup.model;

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
public class IDMGroupEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String name = "group";
    private String description = "awesome group";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenNameIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of group must not be empty");
        new IDMGroup(null, description);
    }

    @Test
    public void createWhenNameIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of group must not be empty");
        new IDMGroup("", description);
    }

    @Test
    public void createWhenDescriptionIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Description of group must not be empty");
        new IDMGroup(name, "");
    }

    @Test
    public void createWhenDescriptionIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Description of group must not be empty");
        new IDMGroup(name, "");
    }

    @Test
    public void saveShouldPersistData() {
        IDMGroup g = this.entityManager.persistFlushFind(new IDMGroup(name, description));
        assertEquals(name, g.getName());
        assertEquals(UserAndGroupStatus.VALID, g.getStatus());
        assertEquals(description, g.getDescription());
    }
}
