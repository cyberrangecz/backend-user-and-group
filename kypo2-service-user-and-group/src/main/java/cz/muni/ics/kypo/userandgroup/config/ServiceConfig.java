package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Configuration
@Import({ResourceServerSecurityConfig.class, ValidationMessagesConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service", "cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.mapping"})
public class ServiceConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}