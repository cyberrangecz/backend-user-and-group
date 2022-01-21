package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableSpringDataWebSupport
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
@EnableRetry
@Import({ResourceServerSecurityConfig.class})
public class WebConfigRestUserAndGroup implements WebMvcConfigurer {

}
