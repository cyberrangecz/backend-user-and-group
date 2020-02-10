package cz.muni.ics.kypo.userandgroup.rest.integrationtests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.*;
import java.nio.charset.Charset;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.mapping", "cz.muni.ics.kypo.userandgroup.service",
        "cz.muni.ics.kypo.userandgroup.api", "cz.muni.ics.kypo.userandgroup.security.service"})
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"},  basePackageClasses = Jsr310JpaConverters.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
public class RestConfigTest {
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
        rT.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return rT;
    }

    @Bean
    @Primary
    @Qualifier("objMapperRESTApi")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    public HttpServletRequest httpServletRequest(){
        return new HttpServletRequestWrapper(new Request(new Connector()));
    }

}
