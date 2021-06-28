package cz.muni.ics.kypo.userandgroup.entities;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents microservice which participates in the system.
 */
@Entity
@Table(name = "microservice")
public class Microservice extends AbstractEntity<Long> {

    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    /**
     * Instantiates a new Microservice.
     */
    public Microservice() {
    }

    /**
     * Instantiates a new Microservice with attributes name and endpoint. Neither of them can be empty.
     *
     * @param name     the name
     * @param endpoint the endpoint
     */
    public Microservice(String name, String endpoint) {
        Assert.hasLength(name, "Name of microservice must not be empty");
        Assert.hasLength(endpoint, "Endpoint of microservice must not be empty");
        Assert.isTrue(!StringUtils.containsWhitespace(endpoint), "Endpoint of microservice must not contain whitespace");
        this.name = name;
        this.endpoint = endpoint;
    }

    /**
     * Gets the ID of the microservice.
     *
     * @return the ID of the microservice
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets the new ID of the microservice.
     *
     * @param id the ID of the microservice.
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets the name of the microservice.
     *
     * @return the name of the microservice.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name of the microservice.
     *
     * @param name the name of the microservice.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets endpoint of the microservice.
     *
     * @return the endpoint of the microservice.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets a new endpoint of the microservice.
     *
     * @param endpoint the endpoint of the microservice.
     */
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
                "id=" + super.getId() +
                ", name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
