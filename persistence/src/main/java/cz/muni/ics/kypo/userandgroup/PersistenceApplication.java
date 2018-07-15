package cz.muni.ics.kypo.userandgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class PersistenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersistenceApplication.class, args);
    }

}
