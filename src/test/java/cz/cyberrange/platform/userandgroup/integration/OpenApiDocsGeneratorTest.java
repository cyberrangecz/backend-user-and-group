package cz.cyberrange.platform.userandgroup.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.userandgroup.definition.config.OpenApiConfiguration;
import cz.cyberrange.platform.userandgroup.rest.controller.GroupsRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.MicroservicesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.RolesRestController;
import cz.cyberrange.platform.userandgroup.rest.controller.UsersRestController;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Writes the springdoc OpenAPI document to the directory given by the {@code docs.output.directory}
 * system property, which the {@code docs} Maven profile sets. Skipped in regular test runs.
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
@EnabledIfSystemProperty(named = "docs.output.directory", matches = ".+")
class OpenApiDocsGeneratorTest {

  private static final String FILE_NAME = "user-and-group-swagger-docs.yaml";

  @Autowired private MockMvc mvc;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Test
  void generateOpenApiDocs() throws Exception {
    String yaml =
        mvc.perform(get(contextPath + "/v3/api-docs.yaml").contextPath(contextPath))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
    Path directory = Paths.get(System.getProperty("docs.output.directory"));
    Files.createDirectories(directory);
    Files.writeString(directory.resolve(FILE_NAME), yaml, StandardCharsets.UTF_8);
  }
}
