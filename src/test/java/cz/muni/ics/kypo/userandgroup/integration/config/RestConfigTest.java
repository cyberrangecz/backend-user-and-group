package cz.muni.ics.kypo.userandgroup.integration.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.nio.charset.StandardCharsets;

@Configuration
@ComponentScan(basePackages = {
        "cz.muni.ics.kypo.userandgroup.facade",
        "cz.muni.ics.kypo.userandgroup.mapping",
        "cz.muni.ics.kypo.userandgroup.service",
        "cz.muni.ics.kypo.userandgroup.handler",
        "cz.muni.ics.kypo.userandgroup.util"
})
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.domain")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@EnableTransactionManagement
@EnableRetry
public class RestConfigTest extends WebSecurityConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);
    @Autowired
    private Environment env;

    @Bean
    public ModelMapper modelMapper() {
        LOG.debug("modelMapper()");
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rT = new RestTemplate();
        rT.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return rT;
    }

    @Bean
    @Primary
    @Qualifier("objMapperRESTApi")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean(name = "yamlObjectMapper")
    public ObjectMapper yamlObjectMapper() {
        ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        yamlObjectMapper.findAndRegisterModules();
        yamlObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        yamlObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        yamlObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return yamlObjectMapper;
    }


    @Bean
    public HttpServletRequest httpServletRequest() {
        return new HttpServletRequestWrapper(new Request(new Connector()));
    }
}
