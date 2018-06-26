package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PersistenceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service"})
public class ServiceConfig {
}
