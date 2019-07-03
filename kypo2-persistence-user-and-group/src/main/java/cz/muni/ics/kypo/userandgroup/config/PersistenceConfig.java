package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.api.config.ApiConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 * @author Jan Duda
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model", "cz.muni.ics.kypo.userandgroup.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.model")
@PropertySource("file:${path.to.config.file}")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
@Import(ApiConfig.class)
public class PersistenceConfig {
}
