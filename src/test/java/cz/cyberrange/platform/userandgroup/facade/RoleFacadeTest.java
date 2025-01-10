package cz.cyberrange.platform.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.rest.facade.RoleFacade;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.RoleType;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapperImpl;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class })
class RoleFacadeTest {

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
    void init() {
        closeable = MockitoAnnotations.openMocks(this);

        roleFacade = new RoleFacade(roleService, roleMapper);

        microservice = testDataFactory.getCrczpUaGMicroservice();
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
    void testGetRoleById() {
        given(roleService.getRoleById(adminRole.getId())).willReturn(adminRole);
        RoleDTO roleDTO = roleFacade.getRoleById(adminRole.getId());
        assertEquals(adminRoleDTO, roleDTO);
    }

    @Test
    void testGetByRoleType() {
        given(roleService.getByRoleType(anyString())).willReturn(adminRole);
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleType.ADMINISTRATOR.toString());

        assertEquals(adminRoleDTO, roleDTO);
    }

    @Test
    void testGetAllRoles() {
        given(roleService.getAllRoles(predicate, pageable)).willReturn(new PageImpl<>(Arrays.asList(adminRole, guestRole)));
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(predicate, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertTrue(pageResultResource.getContent().contains(adminRoleDTO));
        assertTrue(pageResultResource.getContent().contains(guestRoleDTO));
    }
}