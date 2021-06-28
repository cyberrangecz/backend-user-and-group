package cz.muni.ics.kypo.userandgroup.rest;

import cz.muni.ics.kypo.userandgroup.rest.config.WebConfigRestUserAndGroup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "cz.muni.ics.kypo.userandgroup.rest")
@Import({WebConfigRestUserAndGroup.class})
@EnableScheduling
public class UserAndGroupApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(UserAndGroupApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UserAndGroupApplication.class, args);
    }

}