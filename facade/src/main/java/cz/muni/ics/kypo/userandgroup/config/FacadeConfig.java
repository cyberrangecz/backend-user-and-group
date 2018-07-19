package cz.muni.ics.kypo.userandgroup.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import(ServiceConfig.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.mapping"})
public class FacadeConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}

