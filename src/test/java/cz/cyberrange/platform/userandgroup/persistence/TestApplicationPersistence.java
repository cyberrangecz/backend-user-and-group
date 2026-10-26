package cz.cyberrange.platform.userandgroup.persistence;

import cz.cyberrange.platform.userandgroup.persistence.config.PersistenceConfiguration;
import cz.cyberrange.platform.userandgroup.util.PostgresContainerConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the domain and
// repository tests
@SpringBootApplication
@Import({PostgresContainerConfiguration.class, PersistenceConfiguration.class})
@ComponentScan(basePackages = "cz.cyberrange.platform.userandgroup.util")
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
class TestApplicationPersistence {}
