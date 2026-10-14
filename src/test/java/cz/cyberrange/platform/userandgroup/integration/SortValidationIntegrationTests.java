package cz.cyberrange.platform.userandgroup.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.rest.controller.GroupsRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.MicroservicesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.RolesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.UsersRestController;
import cz.cyberrange.platform.userandgroup.rest.handler.CustomRestExceptionHandler;
import cz.cyberrange.platform.userandgroup.rest.sorting.SortableProperties;
import cz.cyberrange.platform.userandgroup.util.TestAuthorityGranter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest(classes = {IntegrationTestApplication.class, UsersRestController.class})
@TestPropertySource("classpath:application.properties")
class SortValidationIntegrationTests {

  private MockMvc mvc;
  @Autowired private UsersRestController usersRestController;

  static Stream<List<String>> acceptedSortParameters() {
    return Stream.of(
        List.of(),
        List.of("fullName,desc", "id"),
        List.of("familyName,givenName,asc"),
        List.of("fullName,ignorecase"));
  }

  static Stream<List<String>> rejectedSortParameters() {
    return Stream.of(
        List.of("fullName,asc", "picture,desc"), List.of("doesNotExist"), List.of(" picture"));
  }

  @BeforeEach
  void init() {
    mvc =
        MockMvcBuilders.standaloneSetup(usersRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(new JacksonJsonHttpMessageConverter())
            .setControllerAdvice(new CustomRestExceptionHandler())
            .build();
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @ParameterizedTest
  @MethodSource("acceptedSortParameters")
  void administratorSortingByListedPropertiesIsAnswered(List<String> sortValues) throws Exception {
    TestAuthorityGranter.mockSpringSecurityContextForGet(
        RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);

    mvc.perform(get("/users").param("sort", sortValues.toArray(String[]::new)))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @MethodSource("rejectedSortParameters")
  void administratorSortingByUnlistedPropertyIsAnsweredWithStatus400(List<String> sortValues)
      throws Exception {
    TestAuthorityGranter.mockSpringSecurityContextForGet(
        RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);

    mvc.perform(get("/users").param("sort", sortValues.toArray(String[]::new)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"));
  }

  @Test
  void rejectionNamesThePropertyAndTheSortableOnes() throws Exception {
    TestAuthorityGranter.mockSpringSecurityContextForGet(
        RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);

    mvc.perform(get("/users").param("sort", "picture,desc"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Cannot sort by 'picture'. Sortable properties: externalId, familyName,"
                        + " fullName, givenName, id, iss, mail, status, sub."));
  }

  @Test
  void callerLackingTheAdministratorRoleIsRefusedBeforeTheSortIsChecked() throws Exception {
    TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);

    String responseBody =
        mvc.perform(get("/users").param("sort", "picture,desc"))
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertFalse(responseBody.contains("Sortable properties"));
  }

  @Test
  void everyPagedControllerMethodListsItsSortableProperties() {
    for (Class<?> controller :
        List.of(
            UsersRestController.class,
            GroupsRestController.class,
            RolesRestController.class,
            MicroservicesRestController.class)) {
      for (Method method : controller.getDeclaredMethods()) {
        if (Arrays.asList(method.getParameterTypes()).contains(Pageable.class)) {
          assertTrue(
              method.isAnnotationPresent(SortableProperties.class),
              controller.getSimpleName() + "." + method.getName());
        }
      }
    }
  }
}
