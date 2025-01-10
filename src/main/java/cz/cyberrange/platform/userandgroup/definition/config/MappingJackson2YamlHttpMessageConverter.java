package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

class MappingJackson2YamlHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    MappingJackson2YamlHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.parseMediaType("application/x-yaml"));
    }
}