package cz.cyberrange.platform.userandgroup.utils.converters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * Serializes a local date-time to JSON as an ISO-8601 instant string, treating it as already being
 * in UTC.
 */
public class LocalDateTimeUTCSerializer extends SerializerBase<LocalDateTime> {

  public LocalDateTimeUTCSerializer() {
    super(LocalDateTime.class);
  }

  /**
   * Writes the given local date-time as an ISO-8601 instant string ending in "Z", treating it as
   * already being in UTC.
   *
   * @param value date-time to write, treated as a UTC instant
   * @param gen generator the value is written to
   * @param provider serializer provider, unused
   * @throws IOException when the value cannot be written
   */
  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(value.toInstant(ZoneOffset.UTC).toString());
  }
}
