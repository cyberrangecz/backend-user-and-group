package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.entities", "cz.muni.ics.kypo.userandgroup.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.entities")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
public class PersistenceConfig { }
