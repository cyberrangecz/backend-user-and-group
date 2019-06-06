package cz.muni.ics.kypo.userandgroup.model;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * @author Pavel Seda
 */
@Entity
@Table(name = "microservice")
public class Microservice {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "endpoint", nullable = false)
    private String endpoint;
    
    public Microservice() {
    }

    public Microservice(String name, String endpoint) {
        Assert.hasLength(name, "Name of microservice must not be empty");
        Assert.hasLength(endpoint, "Endpoint of microservice must not be empty");
        Assert.isTrue(!StringUtils.containsWhitespace(endpoint), "Endpoint of microservice must not contain whitespace");
        this.name = name;
        this.endpoint = endpoint;
    }

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
    public boolean equals(Object object) {
        if (!(object instanceof Microservice)) {
            return false;
        }
        Microservice other = (Microservice) object;
        return Objects.equals(getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "Microservice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
