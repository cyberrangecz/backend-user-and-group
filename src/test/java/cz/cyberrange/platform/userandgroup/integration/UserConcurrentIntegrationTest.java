package cz.cyberrange.platform.userandgroup.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapperImpl;
import cz.cyberrange.platform.userandgroup.api.mapping.UserMapperImpl;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.IdenticonService;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import cz.cyberrange.platform.userandgroup.service.SecurityService;
import cz.cyberrange.platform.userandgroup.service.UserService;
import cz.cyberrange.platform.userandgroup.util.PostgresContainerConfiguration;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(
    classes = {
      UserFacade.class,
      TestDataFactory.class,
      PostgresContainerConfiguration.class,
      UserService.class,
      IDMGroupService.class,
      RoleService.class,
      IdenticonService.class,
      SecurityService.class,
      UserMapperImpl.class,
      RoleMapperImpl.class
    })
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository")
@EnableTransactionManagement
@EnableResilientMethods
class UserConcurrentIntegrationTest {

  private static final int THREAD_COUNT = 50;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private UserFacade userFacade;
  @Autowired private UserRepository userRepository;
  @Autowired private IDMGroupRepository idmGroupRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private MicroserviceRepository microserviceRepository;

  @BeforeEach
  void init() {
    Microservice userAndGroupMicroservice = testDataFactory.getCrczpUaGMicroservice();
    microserviceRepository.save(userAndGroupMicroservice);

    Role role = testDataFactory.getUAGTraineeRole();
    role.setMicroservice(userAndGroupMicroservice);
    roleRepository.save(role);

    IDMGroup defaultGroup = testDataFactory.getUAGDefaultGroup();
    defaultGroup.setRoles(Set.of(role));
    idmGroupRepository.save(defaultGroup);
  }

  @AfterEach
  void cleanUp() {
    idmGroupRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();
    microserviceRepository.deleteAll();
  }

  @Test
  void createOrUpdateUser() throws Exception {
    List<Callable<UserDTO>> logins = new ArrayList<>();
    for (int thread = 0; thread < THREAD_COUNT; thread++) {
      logins.add(() -> userFacade.createOrUpdateOrGetOIDCUser(getOIDCUserInfo()));
    }

    List<UserDTO> loggedInUsers = new ArrayList<>();
    try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
      for (Future<UserDTO> login : executor.invokeAll(logins)) {
        loggedInUsers.add(login.get());
      }
    }

    for (UserDTO loggedInUser : loggedInUsers) {
      assertEquals("Ing. Michael Johnson", loggedInUser.getFullName());
      assertEquals("mail@test.cz", loggedInUser.getSub());
      assertEquals("oidc.provider.cz", loggedInUser.getIss());
    }
    List<User> users = userRepository.findAll();
    assertEquals(1, users.size());
    assertEquals("Ing. Michael Johnson", users.get(0).getFullName());
  }

  private UserCreateDTO getOIDCUserInfo() {
    UserCreateDTO oidcUserInfo = new UserCreateDTO();
    oidcUserInfo.setSub("mail@test.cz");
    oidcUserInfo.setIss("oidc.provider.cz");
    oidcUserInfo.setFullName("Ing. Michael Johnson");
    oidcUserInfo.setGivenName("Michael");
    oidcUserInfo.setFamilyName("Johnson");
    return oidcUserInfo;
  }
}
