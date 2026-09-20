package cz.cyberrange.platform.userandgroup.utils.converters;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

/** Deserializes a JSON string into a local date-time in UTC. */
public class LocalDateTimeUTCDeserializer extends StdDeserializer<LocalDateTime> {

  public LocalDateTimeUTCDeserializer() {
    super(LocalDateTime.class);
  }

  /**
   * Parses the current JSON value as an ISO-8601 instant ending in "Z" and returns it as a local
   * date-time in UTC.
   *
   * @param jp parser positioned at the string to parse
   * @param ctxt deserialization context, unused
   * @return the parsed value as a UTC local date-time
   * @throws IOException when the value cannot be read as a string
   * @throws java.time.format.DateTimeParseException when the value is not a valid instant string
   */
  @Override
  public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    Instant instant = Instant.parse(jp.readValueAs(String.class));
    return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
  }
}
