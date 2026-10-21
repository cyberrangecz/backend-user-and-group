package cz.cyberrange.platform.userandgroup.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.config.OpenApiConfiguration;
import cz.cyberrange.platform.userandgroup.rest.controller.GroupsRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.MicroservicesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.RolesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.UsersRestController;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Checks the OpenAPI document served for a QueryDSL-filtered, paged operation: it documents the
 * required {@code predicate} query parameter next to the enumerated {@code sort} values, and its
 * server URL carries the context path. Clears the static ignore of {@code Predicate} that other
 * Spring contexts in the same JVM register, so the result does not depend on test order.
 */
@SpringBootTest(
    classes = {
      IntegrationTestApplication.class,
      OpenApiConfiguration.class,
      GroupsRestController.class,
      MicroservicesRestController.class,
      RolesRestController.class,
      UsersRestController.class
    })
@TestPropertySource("classpath:application.properties")
@AutoConfigureMockMvc(addFilters = false)
class OpenApiFilterAndSortDocumentationTest {

  private static final String USERS_PARAMETERS = "$.paths['/users'].get.parameters";

  @Autowired private MockMvc mvc;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @BeforeAll
  static void restoreQuerydslPredicateDocumentation() {
    SpringDocUtils.getConfig().removeRequestWrapperToIgnore(Predicate.class);
  }

  private String servedDocument() throws Exception {
    return mvc.perform(get(contextPath + "/v3/api-docs").contextPath(contextPath))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void filteredOperationDocumentsRequiredPredicateAlongsideSortValues() throws Exception {
    String document = servedDocument();

    List<Boolean> predicateRequired =
        JsonPath.read(document, USERS_PARAMETERS + "[?(@.name=='predicate')].required");
    List<String> sortValues =
        JsonPath.read(document, USERS_PARAMETERS + "[?(@.name=='sort')].schema.items.enum[*]");

    assertEquals(List.of(true), predicateRequired);
    assertTrue(sortValues.containsAll(List.of("fullName,asc", "fullName,desc", "id,asc")));
  }

  @Test
  void userListingDocumentsNameFilter() throws Exception {
    List<String> descriptions =
        JsonPath.read(servedDocument(), USERS_PARAMETERS + "[?(@.name=='name')].description");

    assertEquals(1, descriptions.size());
    assertTrue(descriptions.get(0).contains("contains every whitespace-separated word"));
  }

  @Test
  void serverUrlCarriesContextPath() throws Exception {
    List<String> serverUrls = JsonPath.read(servedDocument(), "$.servers[*].url");

    assertEquals(List.of("http://localhost" + contextPath), serverUrls);
  }
}
