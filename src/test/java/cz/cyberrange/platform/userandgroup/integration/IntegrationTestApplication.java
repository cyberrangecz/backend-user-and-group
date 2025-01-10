package cz.cyberrange.platform.userandgroup.integration;

import cz.cyberrange.platform.userandgroup.integration.config.RestConfigTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RestConfigTest.class)
class IntegrationTestApplication {
}
