package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import({DBModelConfig.class})
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.persistence")
public class PersistenceConfig {
}
