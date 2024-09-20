package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.enums.dto.RoleTypeDTO;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.service.RoleService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class })
public class RoleFacadeTest {

    @Autowired
    private TestDataFactory testDataFactory;
    private RoleFacade roleFacade;
    @Mock
    private RoleService roleService;
    @Autowired
    private RoleMapperImpl roleMapper;

    private AutoCloseable closeable;
    private Role adminRole, guestRole;
    private Microservice microservice;
    private RoleDTO adminRoleDTO, guestRoleDTO;
    private Predicate predicate;
    private Pageable pageable;

    @BeforeEach
    public void init() {
        closeable = MockitoAnnotations.openMocks(this);

        roleFacade = new RoleFacade(roleService, roleMapper);

        microservice = testDataFactory.getKypoUaGMicroservice();
        microservice.setId(1L);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        adminRole.setMicroservice(microservice);
        guestRole = testDataFactory.getUAGTraineeRole();
        guestRole.setId(2L);
        guestRole.setMicroservice(microservice);

        adminRoleDTO = testDataFactory.getUAGAdminRoleDTO();
        adminRoleDTO.setId(1L);
        adminRoleDTO.setNameOfMicroservice(adminRole.getMicroservice().getName());
        guestRoleDTO = testDataFactory.getUAGTraineeRoleDTO();
        guestRoleDTO.setId(2L);
        guestRoleDTO.setNameOfMicroservice(guestRole.getMicroservice().getName());

        pageable = PageRequest.of(0, 10);
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
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