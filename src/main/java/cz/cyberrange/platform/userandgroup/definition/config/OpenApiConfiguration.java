package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Optional;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declares the OpenAPI document and schema generation used by the REST API. */
@Configuration
public class OpenApiConfiguration {

  private static final String BEARER_SECURITY_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI openApi(Optional<BuildProperties> buildProperties) {
    Info info =
        new Info()
            .title("CyberRangeCZ Platform User And Group - API Reference")
            .version(buildProperties.map(BuildProperties::getVersion).orElse(null));
    return new OpenAPI()
        .info(info)
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SECURITY_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Bearer token issued by the configured OIDC provider.")));
  }

  /**
   * Resolves schema property names in snake_case, matching the names written in JSON bodies.
   *
   * @return the schema model resolver
   */
  @Bean
  public ModelResolver modelResolver() {
    ObjectMapper schemaObjectMapper = new ObjectMapper();
    schemaObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    return new ModelResolver(schemaObjectMapper);
  }
}
