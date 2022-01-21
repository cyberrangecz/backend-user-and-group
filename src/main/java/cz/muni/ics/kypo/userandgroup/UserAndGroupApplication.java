package cz.muni.ics.kypo.userandgroup;

import cz.muni.ics.kypo.userandgroup.config.WebConfigRestUserAndGroup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({WebConfigRestUserAndGroup.class})
public class UserAndGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAndGroupApplication.class, args);
    }
}