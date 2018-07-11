package cz.muni.ics.kypo.userandgroup.rest.config;

import cz.muni.ics.kypo.userandgroup.config.ServiceConfig;
import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


//@SpringBootApplication
//@EnableSpringDataWebSupport
@Import({ServiceConfig.class, SwaggerConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.rest.mapping"})
public class RestConfig {// extends SpringBootServletInitializer {

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(RestConfig.class);
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(RestConfig.class, args);
//    }
//
//    @Bean
//    @Primary
//    public MappingJackson2HttpMessageConverter jacksonHTTPMessageConverter() {
//        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
//        jsonConverter.setObjectMapper(objectMapperForRestAPI());
//        return jsonConverter;
//    }
//
//    @Bean(name = "objMapperRESTApi")
//    @Primary
//    public ObjectMapper objectMapperForRestAPI() {
//        ObjectMapper obj = new ObjectMapper();
//        obj.setPropertyNamingStrategy(snakeCase());
//        return obj;
//    }
//
//    /**
//     * Naming strategy for returned JSONs.
//     *
//     * @return Naming Strategy for JSON properties
//     */
//    @Bean(name = "properyNamingSnakeCase")
//    public PropertyNamingStrategy snakeCase() {
//        return PropertyNamingStrategy.SNAKE_CASE;
//    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
