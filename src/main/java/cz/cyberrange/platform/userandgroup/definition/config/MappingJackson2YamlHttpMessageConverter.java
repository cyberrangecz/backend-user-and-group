package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

/** Reads and writes YAML request and response bodies using the given object mapper. */
class MappingJackson2YamlHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

  MappingJackson2YamlHttpMessageConverter(ObjectMapper objectMapper) {
    super(objectMapper, MediaType.parseMediaType("application/x-yaml"));
  }
}
