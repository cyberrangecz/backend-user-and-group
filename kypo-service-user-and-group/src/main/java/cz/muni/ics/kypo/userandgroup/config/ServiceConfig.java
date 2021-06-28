package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
@Import({PersistenceConfig.class, ValidationMessagesConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service", "cz.muni.ics.kypo.userandgroup.config", "cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.mapping", "cz.muni.ics.kypo.userandgroup.startuprunners"})
public class ServiceConfig {

}
