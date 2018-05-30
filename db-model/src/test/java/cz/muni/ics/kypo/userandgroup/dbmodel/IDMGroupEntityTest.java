package cz.muni.ics.kypo.userandgroup.dbmodel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
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
    private UserAndGroupStatus status = UserAndGroupStatus.VALID;
    private String description = "aswesome group";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenNameIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of group must not be empty");
        new IDMGroup(null, status, description);
    }

    @Test
    public void createWhenNameIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Name of group must not be empty");
        new IDMGroup("", status, description);
    }

    @Test
    public void createWhenStatusIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Status of group must not be null");
        new IDMGroup(name, null, description);
    }

    @Test
    public void createWhenDescriptionIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Description of group must not be empty");
        new IDMGroup(name, status, "");
    }

    @Test
    public void createWhenDescriptionIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Description of group must not be empty");
        new IDMGroup(name, status, "");
    }

    @Test
    public void saveShouldPersistData() {
        IDMGroup g = this.entityManager.persistFlushFind(new IDMGroup(name, status, description));
        assertEquals(name, g.getName());
        assertEquals(status, g.getStatus());
        assertEquals(description, g.getDescription());
    }
}
