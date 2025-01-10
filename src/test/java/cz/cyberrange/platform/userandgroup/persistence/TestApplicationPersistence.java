package cz.cyberrange.platform.userandgroup.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the domain and repository tests
@SpringBootApplication
@ComponentScan(basePackages = "cz.cyberrange.platform.userandgroup.util")
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository")
class TestApplicationPersistence {

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
