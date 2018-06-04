package cz.muni.ics.kypo.userandgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.persistence"})
public class UserAndGroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAndGroupApplication.class, args);
    }
}
