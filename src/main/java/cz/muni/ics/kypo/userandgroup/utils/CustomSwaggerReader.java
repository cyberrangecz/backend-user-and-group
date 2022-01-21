package cz.muni.ics.kypo.userandgroup.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.apache.maven.plugin.logging.Log;

public class CustomSwaggerReader extends SpringMvcApiReader {

    public CustomSwaggerReader(Swagger swagger, Log log) {
        super(swagger, log);
        Json.mapper().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    }
}
