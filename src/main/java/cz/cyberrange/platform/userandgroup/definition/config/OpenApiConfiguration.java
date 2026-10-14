package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.userandgroup.rest.sorting.SortParameterOpenApiCustomizer;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Optional;
import org.springdoc.core.customizers.QuerydslPredicateOperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.web.method.HandlerMethod;

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

  /**
   * Registers the customizer that documents the allowed values, format, default order and {@code
   * id} tiebreaker of each paged operation's {@code sort} parameter.
   *
   * @return the customizer applied to every operation
   */
  @Bean
  public SortParameterOpenApiCustomizer sortParameterOpenApiCustomizer() {
    return new SortParameterOpenApiCustomizer();
  }

  /**
   * Builds the document generator's QueryDSL operation customizer as one that leaves operations
   * unchanged, so a QueryDSL predicate argument is documented as the single {@code predicate} query
   * parameter rather than as one query parameter per field of the predicate's root entity.
   *
   * @param querydslBindingsFactory the factory of the QueryDSL bindings predicates are bound with
   * @param springDocConfigProperties the document generator's settings
   * @return the customizer registered in place of the generator's default one
   */
  @Bean
  public QuerydslPredicateOperationCustomizer querydslPredicateOperationCustomizer(
      QuerydslBindingsFactory querydslBindingsFactory,
      SpringDocConfigProperties springDocConfigProperties) {
    return new QuerydslPredicateOperationCustomizer(
        querydslBindingsFactory, springDocConfigProperties) {
      @Override
      public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        return operation;
      }
    };
  }
}
