package cz.muni.ics.kypo.userandgroup.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the domain and repository tests
@SpringBootApplication
@ComponentScan(basePackages = "cz.muni.ics.kypo.userandgroup.util")
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.domain")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
public class TestApplicationPersistence {

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
