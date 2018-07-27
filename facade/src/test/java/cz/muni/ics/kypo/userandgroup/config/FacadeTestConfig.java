package cz.muni.ics.kypo.userandgroup.config;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacadeTestConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FacadeTestConfig.class);

    @Bean
    public ModelMapper modelMapper() {
        LOG.debug("modelMapper()");
        return new ModelMapper();
    }

}

