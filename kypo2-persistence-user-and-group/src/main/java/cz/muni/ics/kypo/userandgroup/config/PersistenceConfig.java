package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model", "cz.muni.ics.kypo.userandgroup.repository", "cz.muni.ics.kypo.userandgroup.startuprunners"})
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.model")
@PropertySource("file:${path.to.config.file}")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
public class PersistenceConfig {
}
