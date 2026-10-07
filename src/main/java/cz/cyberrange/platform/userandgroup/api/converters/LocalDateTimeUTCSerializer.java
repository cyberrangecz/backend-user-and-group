package cz.cyberrange.platform.userandgroup.api.converters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Writes a {@link LocalDateTime} as a JSON string in ISO-8601 instant form with exactly 3
 * fractional digits and a trailing {@code Z}, treating the value's date and time fields as already
 * being at offset UTC rather than converting them from another zone. A null value never reaches
 * here and is written as a JSON null.
 */
public class LocalDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

  private static final DateTimeFormatter UTC_INSTANT_FORMATTER =
      new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

  public LocalDateTimeUTCSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) {
    gen.writeString(UTC_INSTANT_FORMATTER.format(value.toInstant(ZoneOffset.UTC)));
  }
}
