package cz.cyberrange.platform.userandgroup.definition.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import cz.cyberrange.platform.userandgroup.security.config.ResourceServerSecurityConfig;
import java.util.List;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures the web MVC message converter used by the REST API's YAML request and response bodies.
 */
@EnableSpringDataWebSupport
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
@EnableRetry
@Import({ResourceServerSecurityConfig.class})
public class WebConfigRestUserAndGroup implements WebMvcConfigurer {

  /**
   * Adds a converter for YAML request and response bodies, with indented output and unknown
   * properties ignored on read.
   *
   * @param converters message converters registered for the REST API; the YAML converter is
   *     appended to this list
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    YAMLMapper mapper = new YAMLMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    converters.add(new MappingJackson2YamlHttpMessageConverter(mapper));
  }
}
