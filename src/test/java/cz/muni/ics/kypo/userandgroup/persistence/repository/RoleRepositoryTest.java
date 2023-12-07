package cz.muni.ics.kypo.userandgroup.persistence.repository;

import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TestDataFactory testDataFactory;
    private Microservice kypoTraining, kypoUserAndGroup;
    private Role adminRole, guestRole, designerRole, traineeRole;
    private IDMGroup group1, group2;
    private User user1, user2;

    @BeforeEach
    public void setup() {
        kypoTraining = testDataFactory.getKypoTrainingMicroservice();
        this.entityManager.persistAndFlush(kypoTraining);

        kypoUserAndGroup = testDataFactory.getKypoUaGMicroservice();
        this.entityManager.persistAndFlush(kypoUserAndGroup);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(kypoUserAndGroup);
        this.entityManager.persistAndFlush(adminRole);

        guestRole = testDataFactory.getUAGTraineeRole();
        guestRole.setMicroservice(kypoUserAndGroup);
        this.entityManager.persistAndFlush(guestRole);

        designerRole = testDataFactory.getTrainingDesignerRole();
        designerRole.setMicroservice(kypoTraining);
        this.entityManager.persistAndFlush(designerRole);

        traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setMicroservice(kypoTraining);
        this.entityManager.persistAndFlush(traineeRole);

        user1 = testDataFactory.getUser1();
        this.entityManager.persistAndFlush(user1);

        user2 = testDataFactory.getUser2();
        this.entityManager.persistAndFlush(user2);

        group1 = testDataFactory.getUAGAdminGroup();
        group1.setRoles(Set.of(adminRole));
        group1.setUsers(Set.of(user1));
        this.entityManager.persistAndFlush(group1);

        group2 = testDataFactory.getUAGPowerUserGroup();
        group2.setRoles(Set.of(guestRole, traineeRole));
        group2.setUsers(Set.of(user1, user2));
        this.entityManager.persistAndFlush(group2);
    }

    @Test
    public void findByRoleType() throws Exception {
        Optional<Role> optionalRole = this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        Role r = optionalRole.orElseThrow(() -> new Exception("Role should be found"));
        assertEquals(adminRole, r);
        assertEquals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString(), r.getRoleType());
    }

    @Test
    public void findByRoleTypeNotFound() {
        assertFalse(this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_POWER_USER.toString()).isPresent());
    }

    @Test
    public void existByRoleType() {
        assertTrue(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString()));
        assertFalse(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_POWER_USER.toString()));
    }

    @Test
    public void getAllRolesByMicroservice() {
        Set<Role> roles = roleRepository.getAllRolesByMicroserviceName(kypoTraining.getName());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(designerRole));
        assertTrue(roles.contains(traineeRole));
        assertFalse(roles.contains(adminRole));
    }

    @Test
    public void getRolesOfUser() {
        Page<Role> user1Roles = roleRepository.findAllOfUser(user1.getId(), null, null);
        assertEquals(3, user1Roles.getContent().size());
        assertTrue(user1Roles.getContent().containsAll(Set.of(adminRole, traineeRole, guestRole)));
        assertFalse(user1Roles.getContent().contains(designerRole));

        Page<Role> user2Roles = roleRepository.findAllOfUser(user2.getId(), null, null);
        assertEquals(2, user2Roles.getContent().size());
        assertTrue(user2Roles.getContent().containsAll(Set.of(traineeRole, guestRole)));
        assertFalse(user2Roles.getContent().containsAll(Set.of(adminRole, designerRole)));
    }

    @Test
    public void getRolesOfGroup() {
        Page<Role> group1Roles = roleRepository.findAllOfGroup(group1.getId(), null, null);
        assertEquals(1, group1Roles.getContent().size());
        assertTrue(group1Roles.getContent().contains(adminRole));
        assertFalse(group1Roles.getContent().containsAll(Set.of(traineeRole, guestRole, designerRole)));

        Page<Role> group2Roles = roleRepository.findAllOfGroup(group2.getId(), null, null);
        assertEquals(2, group2Roles.getContent().size());
        assertTrue(group2Roles.getContent().containsAll(Set.of(traineeRole, guestRole)));
        assertFalse(group2Roles.getContent().containsAll(Set.of(adminRole, designerRole)));
    }

    @Test
    public void finDefaultRoleOfMicroservice() throws Exception {
        IDMGroup defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setRoles(Set.of(guestRole, traineeRole));
        this.entityManager.persistAndFlush(defaultGroup);

        Role defaultUAGRole = roleRepository.findDefaultRoleOfMicroservice(kypoUserAndGroup.getName())
                .orElseThrow(() -> new Exception("Role should be found"));
        assertEquals(defaultUAGRole, guestRole);
        Role defaultTrainingRole = roleRepository.findDefaultRoleOfMicroservice(kypoTraining.getName())
                .orElseThrow(() -> new Exception("Role should be found"));
        assertEquals(defaultTrainingRole, traineeRole);
    }
}
