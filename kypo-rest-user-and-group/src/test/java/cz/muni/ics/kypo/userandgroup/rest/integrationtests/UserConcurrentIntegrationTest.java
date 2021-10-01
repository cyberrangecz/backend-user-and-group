package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserCreateDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(ConcurrentTestRunner.class)
@ContextConfiguration(classes = {UserFacade.class, TestDataFactory.class})
@DataJpaTest
@Import(RestConfigTest.class)
public class UserConcurrentIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IDMGroupRepository idmGroupRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MicroserviceRepository microserviceRepository;

    private UserDTO userDTO;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() throws Exception {
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        userDTO = new UserDTO();
        userDTO.setFullName("Ing. Michael Johnson");
        userDTO.setFamilyName("Johnson");
        userDTO.setGivenName("Michael");
        userDTO.setSub("mail@muni.cz");
        userDTO.setIss("oidc.ics.muni.cz");

        Microservice userAndGroupMicroservice = testDataFactory.getKypoUaGMicroservice();
        microserviceRepository.save(userAndGroupMicroservice);

        Role role = testDataFactory.getUAGGuestRole();
        role.setMicroservice(userAndGroupMicroservice);
        roleRepository.save(role);

        IDMGroup defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setRoles(Set.of(role));
        idmGroupRepository.save(defaultGroup);
    }

    @Test
    @ThreadCount(50)
    public void createOrUpdateUser() throws Exception {
        UserDTO createdUserDTO = userFacade.createOrUpdateOrGetOIDCUser(getOIDCUserInfo());
        assertEquals(userDTO.getFullName(), createdUserDTO.getFullName());
        assertEquals(userDTO.getSub(), createdUserDTO.getSub());
        assertEquals(userDTO.getIss(), createdUserDTO.getIss());
    }

    @After
    public void testCreatedUser() throws Exception {
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("Ing. Michael Johnson", users.get(0).getFullName());
    }

    private UserCreateDTO getOIDCUserInfo() {
        UserCreateDTO oidcUserInfo = new UserCreateDTO();
        oidcUserInfo.setSub("mail@muni.cz");
        oidcUserInfo.setIss("oidc.ics.muni.cz");
        oidcUserInfo.setFullName("Ing. Michael Johnson");
        oidcUserInfo.setGivenName("Michael");
        oidcUserInfo.setFamilyName("Johnson");
        return oidcUserInfo;
    }
}
