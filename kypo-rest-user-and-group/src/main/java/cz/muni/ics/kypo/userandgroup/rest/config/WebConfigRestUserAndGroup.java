package cz.muni.ics.kypo.userandgroup.rest.config;

import cz.muni.ics.kypo.userandgroup.config.FacadeConfig;
import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableSpringDataWebSupport
@Import({ResourceServerSecurityConfig.class, FacadeConfig.class})
public class WebConfigRestUserAndGroup implements WebMvcConfigurer {

}
