package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.RoleTypeDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class})
@ContextConfiguration(classes = {TestDataFactory.class})
public class RoleFacadeTest {

    @Autowired
    private TestDataFactory testDataFactory;
    private RoleFacade roleFacade;
    @Mock
    private RoleService roleService;
    @Autowired
    private RoleMapperImpl roleMapper;

    private Role adminRole, guestRole;
    private RoleDTO adminRoleDTO, guestRoleDTO;
    private Predicate predicate;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        roleFacade = new RoleFacadeImpl(roleService, roleMapper);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setId(2L);

        adminRoleDTO = testDataFactory.getuAGAdminRoleDTO();
        adminRoleDTO.setId(1L);
        adminRoleDTO.setNameOfMicroservice(adminRole.getMicroservice().getName());
        guestRoleDTO = testDataFactory.getuAGGuestRoleDTO();
        guestRoleDTO.setId(2L);
        guestRoleDTO.setNameOfMicroservice(guestRole.getMicroservice().getName());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetRoleById() {
        given(roleService.getRoleById(adminRole.getId())).willReturn(adminRole);
        RoleDTO roleDTO = roleFacade.getRoleById(adminRole.getId());
        assertEquals(adminRoleDTO, roleDTO);
    }

    @Test
    public void testGetByRoleType() {
        given(roleService.getByRoleType(anyString())).willReturn(adminRole);
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleTypeDTO.ADMINISTRATOR.toString());

        assertEquals(adminRoleDTO, roleDTO);
    }

    @Test
    public void testGetAllRoles() {
        given(roleService.getAllRoles(predicate, pageable)).willReturn(new PageImpl<>(Arrays.asList(adminRole, guestRole)));
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertTrue(pageResultResource.getContent().contains(adminRoleDTO));
        assertTrue(pageResultResource.getContent().contains(guestRoleDTO));
    }
}