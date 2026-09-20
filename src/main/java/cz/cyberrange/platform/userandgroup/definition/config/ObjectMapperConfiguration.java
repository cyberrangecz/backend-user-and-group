package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Provides the object mappers used to convert between Java objects and JSON or YAML. */
@Configuration
public class ObjectMapperConfiguration {

  /**
   * Returns the primary object mapper for JSON: renames properties to snake case, registers the
   * Java time module, and writes dates as text instead of timestamps, with indented output.
   *
   * @return the configured object mapper
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return objectMapper;
  }

  /**
   * Returns the object mapper for YAML: registers every auto-detected module, ignores unknown
   * properties on read, and writes dates as text instead of timestamps, with indented output.
   *
   * @return the configured YAML object mapper
   */
  @Bean(name = "yamlObjectMapper")
  public ObjectMapper yamlObjectMapper() {
    ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    yamlObjectMapper.findAndRegisterModules();
    yamlObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    yamlObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    yamlObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return yamlObjectMapper;
  }
}
