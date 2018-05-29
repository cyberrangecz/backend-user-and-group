package cz.muni.ics.kypo.userandgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
public class DBModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DBModelApplication.class, args);
    }
}
