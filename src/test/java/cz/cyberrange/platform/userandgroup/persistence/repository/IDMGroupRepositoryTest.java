package cz.cyberrange.platform.userandgroup.persistence.repository;

import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class IDMGroupRepositoryTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IDMGroupRepository groupRepository;

    private IDMGroup group1, group2, group3, group4;
    private User user1, user2, user3, user4;
    private Role adminRole, powerUserRole, traineeRole, designerRole;
    private Microservice uagMicroservice, trainingMicroservice;
    private Pageable pageable;

    @BeforeEach
    public void init() {
        uagMicroservice = testDataFactory.getCrczpUaGMicroservice();
        trainingMicroservice = testDataFactory.getCrczpTrainingMicroservice();
        this.entityManager.persistAndFlush(uagMicroservice);
        this.entityManager.persistAndFlush(trainingMicroservice);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(uagMicroservice);
        this.entityManager.persistAndFlush(adminRole);

        traineeRole = testDataFactory.getUAGTraineeRole();
        traineeRole.setMicroservice(uagMicroservice);
        this.entityManager.persistAndFlush(traineeRole);

        powerUserRole = testDataFactory.getUAGPowerUserRole();
        powerUserRole.setMicroservice(uagMicroservice);
        this.entityManager.persistAndFlush(powerUserRole);

        designerRole = testDataFactory.getTrainingDesignerRole();
        designerRole.setMicroservice(trainingMicroservice);
        this.entityManager.persistAndFlush(designerRole);

        group1 = testDataFactory.getUAGAdminGroup();
        group2 = testDataFactory.getUAGDefaultGroup();
        group3 = testDataFactory.getUAGPowerUserGroup();
        group4 = testDataFactory.getTrainingDesignerGroup();
        group1.setRoles(new HashSet<>(Set.of(adminRole)));
        group2.setRoles(new HashSet<>(Set.of(traineeRole)));
        group3.setRoles(new HashSet<>(Set.of(powerUserRole)));
        group4.setRoles(new HashSet<>(Set.of(designerRole)));

        pageable = PageRequest.of(0, 10);

        user1 = testDataFactory.getUser1();
        user2 = testDataFactory.getUser2();
        user3 = testDataFactory.getUser3();
        user4 = testDataFactory.getUser4();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.persistAndFlush(user4);
    }

    @Test
    public void findByName() throws Exception {
        this.entityManager.persistAndFlush(group1);
        Optional<IDMGroup> optionalGroup = this.groupRepository.findByName(group1.getName());
        IDMGroup g = optionalGroup.orElseThrow(() -> new Exception("Group should be found"));
        assertEquals(group1, g);
    }

    @Test
    public void findByNameNotFound() {
        assertFalse(this.groupRepository.findByName("group").isPresent());
    }

    @Test
    public void findAllByRoleType() {
        this.entityManager.persistAndFlush(group1);
        this.entityManager.persistAndFlush(group2);
        this.entityManager.persistAndFlush(group4);
        group3.addRole(adminRole);
        this.entityManager.persistAndFlush(group3);

        List<IDMGroup> groups = groupRepository.findAllByRoleType(adminRole.getRoleType());
        assertEquals(2, groups.size());
        assertTrue(groups.containsAll(Set.of(group1, group3)));
        assertFalse(groups.containsAll(Set.of(group2, group4)));
    }

    @Test
    public void findAdministratorGroup() throws Exception {
        entityManager.persistAndFlush(group1);

        Optional<IDMGroup> optionalGroup = groupRepository.findAdministratorGroup();
        IDMGroup g = optionalGroup.orElseThrow(() -> new Exception("Administrator group should be found"));
        assertEquals(this.group1, g);
        assertEquals(this.group1.getName(), g.getName());
    }

    @Test
    public void getIDMGroupByNameWithUsers() throws Exception {
        group1.setUsers(Set.of(user1, user2, user4));
        this.entityManager.persist(group1);

        Optional<IDMGroup> group = this.groupRepository.getIDMGroupByNameWithUsers(group1.getName());
        IDMGroup g = group.orElseThrow(Exception::new);

        assertEquals(group1, g);
        assertEquals(group1.getUsers().size(), g.getUsers().size());
        assertTrue(g.getUsers().containsAll(group1.getUsers()));
    }

    @Test
    public void deleteExpiredIDMGroups() {
        group3.setExpirationDate(LocalDateTime.now().minusDays(5));
        entityManager.persistAndFlush(group3);
        Long groupId = group3.getId();
        entityManager.detach(group3);
        this.groupRepository.deleteExpiredIDMGroups();
        assertFalse(this.groupRepository.findById(groupId).isPresent());
    }

    @Test
    public void findUsersOfGivenGroups() {
        group1.setUsers(Set.of(user4, user3));
        group4.setUsers(Set.of(user2, user3));
        group2.setUsers(Set.of(user1, user2));
        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        entityManager.persistAndFlush(group3);
        entityManager.persistAndFlush(group4);

        Set<User> users = this.groupRepository.findUsersOfGivenGroups(List.of(group1.getId(), group4.getId()));
        assertEquals(3, users.size());
        assertTrue(users.containsAll(Set.of(user2, user3, user4)));
        assertFalse(users.contains(user1));
    }

    @Test
    public void getIDMGroupByNameWithUsersNotFound() throws Exception {
        Optional<IDMGroup> group = this.groupRepository.getIDMGroupByNameWithUsers("group");
        if (group.isPresent()) {
            throw new Exception("Group with name 'group' should not be found");
        }
    }
}
