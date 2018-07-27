package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.config.FacadeTestConfig;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
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
import org.springframework.context.annotation.Import;
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
@ComponentScan(basePackages = {
        "cz.muni.ics.kypo.userandgroup.facade",
        "cz.muni.ics.kypo.userandgroup.service",
        "cz.muni.ics.kypo.userandgroup.mapping"
})
@Import(FacadeTestConfig.class)
public class RoleFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private RoleFacade roleFacade;

    @MockBean
    private RoleService roleService;

    @MockBean
    private IDMGroupService idmGroupService;

    private Role r1 ;
    private RoleDTO roleDTO1;
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
    public void testDeleteRole() {
        given(roleService.getById(anyLong())).willReturn(r1);
        roleFacade.deleteRole(1L);
        then(roleService).should().delete(r1);
    }

    @Test
    public void testGetById() {
        given(roleService.getById(anyLong())).willReturn(r1);
        RoleDTO roleDTO = roleFacade.getById(1L);

        assertEquals(r1.getRoleType(), roleDTO.getRoleType());

    }

    @Test
    public void testGetByIdWithServiceException() {
        given(roleService.getById(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        roleFacade.getById(1L);
    }

    @Test
    public void testGetByRoleType() {
        given(roleService.getByRoleType(anyString())).willReturn(r1);
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleType.ADMINISTRATOR);

        assertEquals(RoleType.ADMINISTRATOR.toString(), roleDTO.getRoleType().toString());
    }

    @Test
    public void testGetByRoleTypeWithServiceException() {
        given(roleService.getByRoleType(anyString())).willThrow(new UserAndGroupFacadeException());
        thrown.expect(UserAndGroupFacadeException.class);
        roleFacade.getByRoleType(RoleType.ADMINISTRATOR);
    }

    @Test
    public void testGetAllRoles() {
        Page<Role> rolePage = new PageImpl<>(Arrays.asList(r1));
        PageResultResource<RoleDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(roleDTO1));

        given(roleService.getAllRoles(predicate, pageable)).willReturn(rolePage);
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate,pageable);

        assertEquals(roleDTO1.toString(), pageResultResource.getContent().get(0).toString());
    }
}
