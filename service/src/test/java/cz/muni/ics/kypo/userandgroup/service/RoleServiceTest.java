package cz.muni.ics.kypo.userandgroup.service;

import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
public class RoleServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private RoleService roleService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private RestTemplate restTemplate;

    private Role adminRole, userRole;

    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());

        userRole = new Role();
        userRole.setRoleType(RoleType.USER.name());
        userRole.setId(2L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void create() {
        given(roleRepository.save(adminRole)).willReturn(adminRole);
        Role r = roleService.create(adminRole);
        assertEquals(adminRole.getId(), r.getId());
        assertEquals(adminRole.getRoleType(), r.getRoleType());

        then(roleRepository).should().save(adminRole);
    }

    @Test
    public void createWithNullRoleShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input role must not be null");
        roleService.create(null);
    }

    @Test
    public void createWithNullRoleTypeShouldThrowException() {
        Role role = new Role();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Role type of input role must not be null");
        roleService.create(role);
    }

    @Test
    public void delete() {
        roleService.delete(adminRole);
        then(roleRepository).should().delete(adminRole);
    }

    @Test
    public void deleteWithNullRoleShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input role must not be null");
        roleService.delete(null);
    }

    @Test
    public void getById() {
        given(roleRepository.findById(adminRole.getId())).willReturn(Optional.of(adminRole));

        Role r = roleService.getById(adminRole.getId());
        assertEquals(adminRole.getId(), r.getId());
        assertEquals(adminRole.getRoleType(), r.getRoleType());

        then(roleRepository).should().findById(adminRole.getId());
    }

    @Test
    public void getByIdNotFoundShouldThrowException() {
        Long id = 3L;
        thrown.expect(IdentityManagementException.class);
        thrown.expectMessage("Role with id " + id + " could not be found");
        given(roleRepository.findById(id)).willReturn(Optional.empty());
        roleService.getById(id);
    }

    @Test
    public void getByIdWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        roleService.getById(null);
    }

    @Test
    public void getByRoleType() {
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.of(adminRole));

        Role r = roleService.getByRoleType(adminRole.getRoleType());
        assertEquals(adminRole.getId(), r.getId());
        assertEquals(adminRole.getRoleType(), r.getRoleType());

        then(roleRepository).should().findByRoleType(adminRole.getRoleType());
    }

    @Test
    public void getByRoleTypeWithNullRoleTypeShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input role type must not be null");
        roleService.getByRoleType(null);
    }

    @Test
    public void getAllRoles() {
        given(roleRepository.findAll(pageable))
                .willReturn(new PageImpl<>(Arrays.asList(adminRole, userRole)));

        Role role = new Role();
        role.setRoleType(RoleType.GUEST.name());

        List<Role> roles = roleService.getAllRoles(pageable).getContent();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(userRole));
        assertFalse(roles.contains(role));

        then(roleRepository).should().findAll(pageable);
    }

    @After
    public void afterMethod() {
        reset(roleRepository);
    }
}
