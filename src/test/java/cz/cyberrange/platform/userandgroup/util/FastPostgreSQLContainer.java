package cz.cyberrange.platform.userandgroup.util;

import java.util.Map;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL test container tuned for speed: durability settings are switched off and the data
 * directory lives in memory, so a crash loses data, which a throwaway test database never needs.
 */
public final class FastPostgreSQLContainer extends PostgreSQLContainer {

  private static final String POSTGRES_IMAGE = "postgres:15";
  private static final String DATA_DIRECTORY = "/var/lib/postgresql/data";

  /** Creates the container without starting it. */
  public FastPostgreSQLContainer() {
    super(POSTGRES_IMAGE);
    withCommand(
        "postgres",
        "-c",
        "fsync=off",
        "-c",
        "synchronous_commit=off",
        "-c",
        "full_page_writes=off");
    withTmpFs(Map.of(DATA_DIRECTORY, "rw"));
  }
}
