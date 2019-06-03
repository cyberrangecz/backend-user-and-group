package cz.muni.ics.kypo.userandgroup.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * This class serialize LocalDateTime to UTC time.
 *
 * @author Pavel Seda
 */
public class LocalDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

    public LocalDateTimeUTCSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toInstant(ZoneOffset.UTC).toString());
    }
}

