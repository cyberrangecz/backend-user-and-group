package cz.muni.ics.kypo.userandgroup.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup"})
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.dbmodel")
public class DBModelConfig {
}
