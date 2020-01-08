package cz.muni.ics.kypo.userandgroup.api.converters;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * This class serialize LocalDateTime to UTC time.
 *
 */
public class LocalDateTimeUTCSerializer extends SerializerBase<LocalDateTime> {

    public LocalDateTimeUTCSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toInstant(ZoneOffset.UTC).toString());
    }
}

