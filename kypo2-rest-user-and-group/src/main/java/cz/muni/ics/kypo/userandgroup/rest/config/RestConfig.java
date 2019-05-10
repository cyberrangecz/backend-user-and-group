package cz.muni.ics.kypo.userandgroup.rest.config;

import cz.muni.ics.kypo.userandgroup.config.ServiceConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport
@Import({ServiceConfig.class, SwaggerConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.rest"})
public class RestConfig {

}
