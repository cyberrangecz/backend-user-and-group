package cz.muni.ics.kypo.userandgroup.facade;


import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade",  "cz.muni.ics.kypo.userandgroup.service"})
public class RoleFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private RoleFacade roleFacade;

    @MockBean
    private RoleService roleService;

    @MockBean
    private IDMGroupService idmGroupService;

    @MockBean
    private BeanMapping beanMapping;

    private Role r1 ;
    private RoleDTO roleDTO1;
    private NewRoleDTO newRoleDTO;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        r1 = new Role();
        r1.setId(1L);
        r1.setRoleType(RoleType.ADMINISTRATOR.toString());

        roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setRoleType(RoleType.ADMINISTRATOR.toString());
    }

    @Test
    public void testCreateRole() {
        given(beanMapping.mapTo(newRoleDTO, Role.class)).willReturn(r1);
        given(beanMapping.mapTo(any(Role.class), eq(RoleDTO.class))).willReturn(roleDTO1);
        given(roleService.create(any(Role.class))).willReturn(r1);
        RoleDTO roleDTO = roleFacade.createRole(newRoleDTO);

        assertEquals(roleDTO1, roleDTO);
        then(roleService).should().create(r1);
    }

    @Test
    public void testDeleteRole() {
        given(roleService.getById(anyLong())).willReturn(r1);
        roleFacade.deleteRole(1L);
        then(roleService).should().delete(r1);
    }

    @Test
    public void testGetById() {
        given(roleService.getById(anyLong())).willReturn(r1);
        given(beanMapping.mapTo(any(Role.class), eq(RoleDTO.class))).willReturn(roleDTO1);
        RoleDTO roleDTO = roleFacade.getById(1L);

        assertEquals(r1.getRoleType(), roleDTO.getRoleType());

    }

    @Test
    public void testGetByIdWithIdentityManagementException() {
        given(roleService.getById(anyLong())).willThrow(new IdentityManagementException());
        thrown.expect(IdentityManagementException.class);
        roleFacade.getById(1L);
    }

    @Test
    public void testGetByRoleType() {
        given(roleService.getByRoleType(anyString())).willReturn(r1);
        given(beanMapping.mapTo(any(Role.class), eq(RoleDTO.class))).willReturn(roleDTO1);
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleType.ADMINISTRATOR);

        assertEquals(RoleType.ADMINISTRATOR.toString(), roleDTO.getRoleType().toString());
    }

    @Test
    public void testGetAllRoles() {
        Page<Role> rolePage = new PageImpl<Role>(Arrays.asList(r1));
        PageResultResource<RoleDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(roleDTO1));

        given(roleService.getAllRoles(predicate, pageable)).willReturn(rolePage);
        given(beanMapping.mapToPageResultDTO(any(Page.class), eq(RoleDTO.class))).willReturn(pageResult);
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate,pageable);

        assertEquals(roleDTO1.toString(), pageResultResource.getContent().get(0).toString());
    }
}
