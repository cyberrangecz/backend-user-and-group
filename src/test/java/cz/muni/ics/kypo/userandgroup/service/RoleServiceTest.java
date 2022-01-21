package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = { TestDataFactory.class })
public class RoleServiceTest {

    private RoleService roleService;
    @MockBean
    private RoleRepository roleRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    private Role adminRole, userRole, guestRole;
    private Pageable pageable;
    private Predicate predicate;

    @BeforeEach
    public void init() {
        roleService = new RoleService(roleRepository);

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

    @Test
    public void getByIdNotFoundShouldThrowException() {
        Long id = 100L;
        given(roleRepository.findById(id)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> roleService.getRoleById(id));
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
    public void getByRoleTypeRoleFound() {
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> roleService.getByRoleType(adminRole.getRoleType()));
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
        given(roleRepository.getAllRolesByMicroserviceName("kypo-training")).willReturn(Set.of(adminRole, userRole));
        Set<Role> roles = roleService.getAllRolesOfMicroservice("kypo-training");
        assertEquals(2, roles.size());
        assertTrue(roles.containsAll(Set.of(adminRole, userRole)));
    }

    @Test
    public void createRole() {
        given(roleRepository.existsByRoleType(userRole.getRoleType())).willReturn(false);
        roleService.createRole(userRole);
        then(roleRepository).should().save(userRole);
    }

    @Test
    public void createExistingRole() {
        given(roleRepository.existsByRoleType(adminRole.getRoleType())).willReturn(true);
        assertThrows(EntityConflictException.class, () -> roleService.createRole(adminRole));
    }

    @AfterEach
    public void afterMethod() {
        reset(roleRepository);
    }
}
