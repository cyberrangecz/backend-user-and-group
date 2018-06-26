package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
public class IDMGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IDMGroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    private IDMGroup group;

    private Role adminRole, userRole, guestRole;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR);

        userRole = new Role();
        userRole.setRoleType(RoleType.USER);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST);

        group = new IDMGroup("groupWithRoles", "Group with roles");
    }

    @Test
    public void findByName() {
        String expectedName = "group";
        String expectedDescription = "Cool group";
        IDMGroup group = new IDMGroup(expectedName, expectedDescription);
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
        IDMGroup group1 = new IDMGroup(expectedName, "cool description");
        IDMGroup group2 = new IDMGroup(expectedName, "awesome description");
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

    @Test
    public void findAdministratorGroup() {
        entityManager.persistFlushFind(adminRole);
        group.addRole(adminRole);
        entityManager.persistFlushFind(group);

        IDMGroup group = groupRepository.findAdministratorGroup();
        assertEquals(this.group, group);
        assertEquals(this.group.getName(), group.getName());
        assertEquals(this.group.getDescription(), group.getDescription());
    }

    @Test
    public void findUserGroup() {
        entityManager.persistFlushFind(userRole);
        group.addRole(userRole);
        entityManager.persistFlushFind(group);

        IDMGroup group = groupRepository.findUserGroup();
        assertEquals(this.group, group);
        assertEquals(this.group.getName(), group.getName());
        assertEquals(this.group.getDescription(), group.getDescription());
    }

    @Test
    public void findGuestGroup() {
        entityManager.persistFlushFind(guestRole);
        group.addRole(guestRole);
        entityManager.persistFlushFind(group);

        IDMGroup group = groupRepository.findGuestGroup();
        assertEquals(this.group, group);
        assertEquals(this.group.getName(), group.getName());
        assertEquals(this.group.getDescription(), group.getDescription());
    }

    @Test
    public void getRolesOfGroup() {
        entityManager.persistFlushFind(adminRole);
        entityManager.persistFlushFind(userRole);
        entityManager.persistFlushFind(guestRole);
        group.setRoles(Stream.of(adminRole, userRole).collect(Collectors.toSet()));
        entityManager.persistFlushFind(group);

        Set<Role> rolesOfGroup = groupRepository.getRolesOfGroup(group.getId());
        assertEquals(2, rolesOfGroup.size());
        assertTrue(rolesOfGroup.contains(adminRole));
        assertTrue(rolesOfGroup.contains(userRole));
        assertFalse(rolesOfGroup.contains(guestRole));
    }
}
