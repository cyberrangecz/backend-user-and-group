package cz.muni.ics.kypo.userandgroup.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the domain and repository tests
@SpringBootApplication
@ComponentScan(basePackages = "cz.muni.ics.kypo.userandgroup.util")
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.domain")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
public class TestApplicationPersistence {
}
