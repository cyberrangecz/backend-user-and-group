package cz.cyberrange.platform.userandgroup.definition.exceptions.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

class ApiErrorTimestampFormatTest {

  private static final JsonMapper JSON_MAPPER =
      JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();

  private static final LocalDateTime MICROSECONDS =
      LocalDateTime.of(2026, 10, 6, 11, 34, 4, 71_345_000);

  @Test
  void writesErrorTimestampAsUtcInstantWithThreeFractionalDigits() {
    ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing", "/groups/1");
    error.setTimestamp(MICROSECONDS);

    assertEquals("2026-10-06T11:34:04.071Z", timestampOf(error));
  }

  @Test
  void padsWholeSecondToThreeFractionalDigits() {
    ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing", "/groups/1");
    error.setTimestamp(LocalDateTime.of(2026, 10, 6, 11, 34, 4));

    assertEquals("2026-10-06T11:34:04.000Z", timestampOf(error));
  }

  @Test
  void writesEntityErrorTimestampInTheSameForm() {
    ApiEntityError error =
        ApiEntityError.of(
            HttpStatus.NOT_FOUND, "Missing", "Missing", "/groups/1", new EntityErrorDetail());
    error.setTimestamp(MICROSECONDS);

    assertEquals("2026-10-06T11:34:04.071Z", timestampOf(error));
  }

  @Test
  void stampsNewErrorWithCurrentUtcTime() {
    LocalDateTime before = LocalDateTime.now(Clock.systemUTC());

    LocalDateTime stamped = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing").getTimestamp();

    assertTrue(Duration.between(before, stamped).abs().compareTo(Duration.ofSeconds(5)) < 0);
  }

  private String timestampOf(ApiError error) {
    return JSON_MAPPER.valueToTree(error).get("timestamp").asString();
  }
}
