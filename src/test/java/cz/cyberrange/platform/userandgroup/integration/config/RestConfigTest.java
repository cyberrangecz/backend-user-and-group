package cz.cyberrange.platform.userandgroup.integration.config;

import java.nio.charset.StandardCharsets;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.userandgroup.rest.facade",
      "cz.cyberrange.platform.userandgroup.api.mapping",
      "cz.cyberrange.platform.userandgroup.service",
      "cz.cyberrange.platform.userandgroup.rest.handler",
      "cz.cyberrange.platform.userandgroup.util"
    })
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository")
@EnableMethodSecurity
@EnableWebSecurity
@EnableTransactionManagement
@EnableResilientMethods
public class RestConfigTest {
  private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);

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
}
