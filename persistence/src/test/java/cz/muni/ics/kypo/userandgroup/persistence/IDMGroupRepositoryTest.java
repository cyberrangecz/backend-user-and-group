package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.*;
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
import java.util.Optional;
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

    private IDMGroup group;

    private Role adminRole, userRole, guestRole;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());

        userRole = new Role();
        userRole.setRoleType(RoleType.USER.name());

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());

        group = new IDMGroup("groupWithRoles", "Group with roles");
    }

    @Test
    public void findByName() throws Exception {
        String expectedName = "group";
        String expectedDescription = "Cool group";
        IDMGroup group = new IDMGroup(expectedName, expectedDescription);
        this.entityManager.persist(group);
        Optional<IDMGroup> optionalGroup = this.groupRepository.findByName(expectedName);
        IDMGroup g = optionalGroup.orElseThrow(() -> new Exception("Group should be found"));
        assertEquals(group, g);
        assertEquals(expectedName, g.getName());
        assertEquals(UserAndGroupStatus.VALID, g.getStatus());
        assertEquals(expectedDescription, g.getDescription());
    }

    @Test
    public void findByNameNotFound() {
        assertFalse(this.groupRepository.findByName("group").isPresent());
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
    public void findAllByRoleType() {
        entityManager.persistFlushFind(adminRole);
        group.addRole(adminRole);
        entityManager.persistFlushFind(group);

        List<IDMGroup> groups = groupRepository.findAllByRoleType(RoleType.ADMINISTRATOR.name());
        assertEquals(1, groups.size());
        assertEquals(this.group.getName(), groups.get(0).getName());
        assertEquals(this.group.getDescription(), groups.get(0).getDescription());
    }

    @Test
    public void findAdministratorGroup() throws Exception {
        entityManager.persistFlushFind(adminRole);
        group.addRole(adminRole);
        entityManager.persistFlushFind(group);

        Optional<IDMGroup> optionalGroup = groupRepository.findAdministratorGroup();
        IDMGroup g = optionalGroup.orElseThrow(() -> new Exception("Administrator group should be found"));
        assertEquals(this.group, g);
        assertEquals(this.group.getName(), g.getName());
        assertEquals(this.group.getDescription(), g.getDescription());
    }


    @Test
    public void getRolesOfGroup() {
        entityManager.persistFlushFind(adminRole);
        entityManager.persistFlushFind(userRole);
        entityManager.persistFlushFind(guestRole);
        group.setRoles(Stream.of(adminRole, userRole).collect(Collectors.toSet()));
        entityManager.persistFlushFind(group);

        Set<Role> rolesOfGroup = groupRepository.getRolesOfGroup(group.getId());
        rolesOfGroup.forEach(role -> System.out.println(role.getRoleType()));
        assertEquals(2, rolesOfGroup.size());
        assertTrue(rolesOfGroup.contains(adminRole));
        assertTrue(rolesOfGroup.contains(userRole));
        assertFalse(rolesOfGroup.contains(guestRole));
    }

    @Test
    public void getIDMGroupByNameWithUsers() throws Exception {
        User user = new User("TestUser");
        this.entityManager.persist(user);

        String expectedName = "group";
        String expectedDescription = "Cool group";
        IDMGroup expectedGroup = new IDMGroup(expectedName, expectedDescription);
        expectedGroup.addUser(user);
        this.entityManager.persist(expectedGroup);

        Optional<IDMGroup> group = this.groupRepository.getIDMGroupByNameWithUsers(expectedName);
        IDMGroup g = group.orElseThrow(Exception::new);

        assertEquals(expectedGroup, g);
        assertEquals(expectedName, g.getName());
        assertEquals(expectedDescription, g.getDescription());
        assertEquals(1, g.getUsers().size());
        assertEquals(user, g.getUsers().get(0));
    }

    @Test
    public void getIDMGroupByNameWithUsersNotFound() throws Exception {
        Optional<IDMGroup> group = this.groupRepository.getIDMGroupByNameWithUsers("group");
        if (group.isPresent()) {
            throw new Exception("Group with name 'group' should not be found");
        }
    }
}
