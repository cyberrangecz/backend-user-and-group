package cz.cyberrange.platform.userandgroup.persistence;

import cz.cyberrange.platform.userandgroup.util.PostgresContainerConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the domain and
// repository tests
@SpringBootApplication
@Import(PostgresContainerConfiguration.class)
@ComponentScan(basePackages = "cz.cyberrange.platform.userandgroup.util")
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository")
class TestApplicationPersistence {}
