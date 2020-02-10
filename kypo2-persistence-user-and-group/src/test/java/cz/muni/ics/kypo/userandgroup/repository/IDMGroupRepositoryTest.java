package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@ComponentScan(basePackages = "cz.muni.ics.kypo.userandgroup.util")
public class IDMGroupRepositoryTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IDMGroupRepository groupRepository;

    private IDMGroup group;

    private Role adminRole, userRole, guestRole;

    private Microservice microservice;

    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        microservice = testDataFactory.getKypoUaGMicroservice();
        this.entityManager.persistAndFlush(microservice);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(adminRole);


        group = testDataFactory.getTrainingAdminGroup();
        group.setRoles(Set.of(adminRole));
        pageable = PageRequest.of(0, 10);
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
    public void findAllByRoleType() {
        entityManager.persistFlushFind(group);

        List<IDMGroup> groups = groupRepository.findAllByRoleType(adminRole.getRoleType());
        assertEquals(1, groups.size());
        assertEquals(this.group.getName(), groups.get(0).getName());
        assertEquals(this.group.getDescription(), groups.get(0).getDescription());
    }

    @Test
    public void findAdministratorGroup() throws Exception {
        entityManager.persistFlushFind(group);

        Optional<IDMGroup> optionalGroup = groupRepository.findAdministratorGroup();
        IDMGroup g = optionalGroup.orElseThrow(() -> new Exception("Administrator group should be found"));
        assertEquals(this.group, g);
        assertEquals(this.group.getName(), g.getName());
        assertEquals(this.group.getDescription(), g.getDescription());
    }


    @Test
    public void getIDMGroupByNameWithUsers() throws Exception {
        User user = new User("TestUser", "https://oidc.muni.cz/oidc/");

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
        assertTrue(g.getUsers().contains(user));
    }

    @Test
    public void getIDMGroupByNameWithUsersNotFound() throws Exception {
        Optional<IDMGroup> group = this.groupRepository.getIDMGroupByNameWithUsers("group");
        if (group.isPresent()) {
            throw new Exception("Group with name 'group' should not be found");
        }
    }

}
