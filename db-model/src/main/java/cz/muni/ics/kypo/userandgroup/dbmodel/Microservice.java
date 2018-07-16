package cz.muni.ics.kypo.userandgroup.dbmodel;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "MICROSERVICE")
public class Microservice {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ENDPOINT", nullable = false)
    private String endpoint;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Microservice)) return false;
        Microservice microservice = (Microservice) o;
        return Objects.equals(name, microservice.name) &&
                Objects.equals(endpoint, microservice.endpoint);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, endpoint);
    }
}
