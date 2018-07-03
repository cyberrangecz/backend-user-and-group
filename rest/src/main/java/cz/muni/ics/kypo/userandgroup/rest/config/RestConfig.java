package cz.muni.ics.kypo.userandgroup.rest.config;


import cz.muni.ics.kypo.userandgroup.config.ServiceConfig;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({ServiceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.rest.mapping"})
public class RestConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
