package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({ResourceServerSecurityConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service"})
public class ServiceConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
