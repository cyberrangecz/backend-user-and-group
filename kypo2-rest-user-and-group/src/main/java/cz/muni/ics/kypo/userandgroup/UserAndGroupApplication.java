package cz.muni.ics.kypo.userandgroup;

import cz.muni.ics.kypo.userandgroup.rest.config.WebConfigRestUserAndGroup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Pavel Seda
 */
@SpringBootApplication
@Import({WebConfigRestUserAndGroup.class})
@EnableScheduling
public class UserAndGroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAndGroupApplication.class, args);
    }

}