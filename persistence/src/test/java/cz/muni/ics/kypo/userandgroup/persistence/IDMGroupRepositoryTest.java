package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class IDMGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IDMGroupRepository groupRepository;

    @Test
    public void findByName() {
        String expectedName = "group";
        String expectedDescription = "Cool group";
        IDMGroup group = new IDMGroup(expectedName, UserAndGroupStatus.VALID, expectedDescription);
        this.entityManager.persist(group);
        IDMGroup g = this.groupRepository.findByName(expectedName);
        assertEquals(group, g);
        assertEquals(expectedName, g.getName());
        assertEquals(UserAndGroupStatus.VALID, g.getStatus());
        assertEquals(expectedDescription, g.getDescription());
    }

    @Test
    public void findByNameNotFound() {
        assertNull(this.groupRepository.findByName("group"));
    }

    @Test
    public void findAllByName() {
        String expectedName = "group";
        IDMGroup group1 = new IDMGroup(expectedName, UserAndGroupStatus.VALID, "cool description");
        IDMGroup group2 = new IDMGroup(expectedName, UserAndGroupStatus.VALID, "awesome description");
        this.entityManager.persist(group1);
        this.entityManager.persist(group2);

        List<IDMGroup> groups = this.groupRepository.findAllByName(expectedName);
        assertEquals(2, groups.size());
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
    }

    @Test
    public void findAllByNameNotFound() {
        List<IDMGroup> groups = this.groupRepository.findAllByName("group");
        assertNotNull(groups);
        assertTrue(groups.isEmpty());
    }
}
