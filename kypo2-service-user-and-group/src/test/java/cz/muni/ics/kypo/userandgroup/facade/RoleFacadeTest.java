package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.RoleTypeDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class})
public class RoleFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private RoleFacade roleFacade;
    @Mock
    private RoleService roleService;
    @Autowired
    private RoleMapperImpl roleMapper;

    private Role r1, r2;
    private Microservice microservice;
    private RoleDTO roleDTO1, roleDTO2;
    private Predicate predicate;
    private Pageable pageable;
    private static final String NAME_OF_USER_AND_GROUP_SERVICE = "kypo2-user-and-group";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        roleFacade = new RoleFacadeImpl(roleService, roleMapper);

        microservice = new Microservice();
        microservice.setId(1L);
        microservice.setName(NAME_OF_USER_AND_GROUP_SERVICE);
        microservice.setEndpoint("/");

        r1 = new Role();
        r1.setId(1L);
        r1.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        r1.setMicroservice(microservice);

        r2 = new Role();
        r2.setId(2L);
        r2.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        r2.setMicroservice(microservice);

        roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        roleDTO1.setNameOfMicroservice(microservice.getName());
        roleDTO1.setIdOfMicroservice(microservice.getId());

        roleDTO2 = new RoleDTO();
        roleDTO2.setId(2L);
        roleDTO2.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        roleDTO2.setNameOfMicroservice(microservice.getName());
        roleDTO2.setIdOfMicroservice(microservice.getId());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetById() {
        given(roleService.getById(anyLong())).willReturn(r1);
        RoleDTO roleDTO = roleFacade.getById(1L);

        assertRoleAndRoleDTO(r1, roleDTO);

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
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleTypeDTO.ADMINISTRATOR.toString());

        assertRoleAndRoleDTO(r1, roleDTO);
    }

    @Test
    public void testGetByRoleTypeWithServiceException() {
        given(roleService.getByRoleType(anyString())).willThrow(new UserAndGroupFacadeException());
        thrown.expect(UserAndGroupFacadeException.class);
        roleFacade.getByRoleType(RoleTypeDTO.ADMINISTRATOR.toString());
    }

    @Test
    public void testGetAllRoles() {
        given(roleService.getAllRoles(predicate, pageable)).willReturn(new PageImpl<>(Arrays.asList(r1, r2)));
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertTrue(pageResultResource.getContent().contains(roleDTO1));
        assertTrue(pageResultResource.getContent().contains(roleDTO2));
    }

    private void assertRoleAndRoleDTO(Role role, RoleDTO roleDTO) {
        assertEquals(role.getId(), roleDTO.getId());
        assertEquals(role.getMicroservice().getId(), roleDTO.getIdOfMicroservice());
        assertEquals(role.getMicroservice().getName(), roleDTO.getNameOfMicroservice());
        assertEquals(role.getRoleType(), roleDTO.getRoleType());
    }
}