package cz.muni.ics.kypo.userandgroup.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import cz.muni.ics.kypo.userandgroup.config.FacadeConfig;
import cz.muni.ics.kypo.userandgroup.config.ServiceConfig;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


@EnableSpringDataWebSupport
@Import({FacadeConfig.class, SwaggerConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.mapping"})
public class RestConfig {

}
