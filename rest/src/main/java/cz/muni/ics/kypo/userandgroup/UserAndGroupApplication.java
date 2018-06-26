package cz.muni.ics.kypo.userandgroup;

import cz.muni.ics.kypo.userandgroup.config.ServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ServiceConfig.class})
public class UserAndGroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAndGroupApplication.class, args);
    }
}
