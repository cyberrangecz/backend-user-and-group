package cz.cyberrange.platform.userandgroup.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Supplies a PostgreSQL container whose connection details replace the test data source. */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new FastPostgreSQLContainer();
  }
}
