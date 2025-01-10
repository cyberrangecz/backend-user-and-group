package cz.cyberrange.platform.userandgroup.persistence.domain;

import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class IDMGroupEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private final String name = "group";
    private final String description = "awesome group";

    @Test
    void createWhenNameIsNullShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new IDMGroup(null, description));
        assertEquals("Name of group must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenNameIsEmptyShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new IDMGroup("", description));
        assertEquals("Name of group must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenDescriptionIsNullShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new IDMGroup(name, null));
        assertEquals("Description of group must not be empty", ex.getMessage());
    }

    @Test
    public void createWhenDescriptionIsEmptyShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new IDMGroup(name, ""));
        assertEquals("Description of group must not be empty", ex.getMessage());
    }

    @Test
    public void saveShouldPersistData() {
        IDMGroup g = this.entityManager.persistFlushFind(new IDMGroup(name, description));
        assertEquals(name, g.getName());
        assertEquals(UserAndGroupStatus.VALID, g.getStatus());
        assertEquals(description, g.getDescription());
    }
}
