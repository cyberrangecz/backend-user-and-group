package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.RoleServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestDataFactory.class)
public class RoleServiceTest {

    private RoleService roleService;
    @MockBean
    private RoleRepository roleRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    private Role adminRole, userRole, guestRole;
    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        roleService = new RoleServiceImpl(roleRepository);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        userRole = testDataFactory.getUAGUserRole();
        userRole.setId(2L);
        guestRole = testDataFactory.getUAGGuestRole();
        userRole.setId(3L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getById() {
        adminRole.setId(1L);
        given(roleRepository.findById(adminRole.getId())).willReturn(Optional.of(adminRole));

        Role r = roleService.getRoleById(adminRole.getId());
        assertEquals(adminRole.getId(), r.getId());
        assertEquals(adminRole.getRoleType(), r.getRoleType());

        then(roleRepository).should().findById(adminRole.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getByIdNotFoundShouldThrowException() {
        Long id = 100L;
        given(roleRepository.findById(id)).willReturn(Optional.empty());
        roleService.getRoleById(id);
    }

    @Test
    public void getByRoleType() {
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.of(adminRole));

        Role r = roleService.getByRoleType(adminRole.getRoleType());
        assertEquals(adminRole.getId(), r.getId());
        assertEquals(adminRole.getRoleType(), r.getRoleType());
        then(roleRepository).should().findByRoleType(adminRole.getRoleType());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getByRoleTypeRoleFound(){
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.empty());
        roleService.getByRoleType(adminRole.getRoleType());
    }

    @Test
    public void getAllRoles() {
        given(roleRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(adminRole, userRole, guestRole)));

        List<Role> roles = roleService.getAllRoles(predicate, pageable).getContent();
        assertEquals(3, roles.size());
        assertTrue(roles.containsAll(Set.of(adminRole, userRole, guestRole)));
        then(roleRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void getAllRolesOfMicroservice() {
        given(roleRepository.getAllRolesByMicroserviceName("kypo2-training")).willReturn(Set.of(adminRole, userRole));
        Set<Role> roles = roleService.getAllRolesOfMicroservice("kypo2-training");
        assertEquals(2, roles.size());
        assertTrue(roles.containsAll(Set.of(adminRole, userRole)));
    }

    @Test
    public void createRole(){
        given(roleRepository.existsByRoleType(userRole.getRoleType())).willReturn(false);
        roleService.createRole(userRole);
        then(roleRepository).should().save(userRole);
    }

    @Test(expected = EntityConflictException.class)
    public void createExistingRole(){
        given(roleRepository.existsByRoleType(adminRole.getRoleType())).willReturn(true);
        roleService.createRole(adminRole);
    }

    @After
    public void afterMethod() {
        reset(roleRepository);
    }
}
