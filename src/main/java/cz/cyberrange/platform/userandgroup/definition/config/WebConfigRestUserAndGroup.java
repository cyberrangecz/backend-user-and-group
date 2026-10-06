package cz.cyberrange.platform.userandgroup.definition.config;

import cz.cyberrange.platform.userandgroup.security.config.ResourceServerSecurityConfig;
import java.util.List;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.yaml.JacksonYamlHttpMessageConverter;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Configures the web MVC message converter used by the REST API's YAML request and response bodies.
 */
@EnableSpringDataWebSupport
@EnableScheduling
@EnableTransactionManagement
@EnableResilientMethods
@Import({ResourceServerSecurityConfig.class})
public class WebConfigRestUserAndGroup implements WebMvcConfigurer {

  private static final MediaType APPLICATION_X_YAML =
      MediaType.parseMediaType("application/x-yaml");

  /**
   * Registers the converter for YAML request and response bodies, read and written as {@code
   * application/x-yaml} or {@code application/yaml}, with properties in declaration order, enums by
   * their constant name, indented output and unknown properties ignored on read.
   *
   * @param builder the builder of the message converters registered for the REST API
   */
  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    YAMLMapper mapper =
        YAMLMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .build();
    JacksonYamlHttpMessageConverter yamlConverter = new JacksonYamlHttpMessageConverter(mapper);
    yamlConverter.setSupportedMediaTypes(List.of(APPLICATION_X_YAML, MediaType.APPLICATION_YAML));
    builder.withYamlConverter(yamlConverter);
  }
}
