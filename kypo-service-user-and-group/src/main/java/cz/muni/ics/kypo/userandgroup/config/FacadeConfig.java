package cz.muni.ics.kypo.userandgroup.config;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableCaching
@EnableTransactionManagement
@ComponentScan("cz.muni.ics.kypo.userandgroup.api.facade")
@Import(ServiceConfig.class)
@EnableRetry
public class FacadeConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
