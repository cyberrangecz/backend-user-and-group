package cz.muni.ics.kypo.userandgroup.api.dto.microservice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about a microservice.
 */
@ApiModel(value = "MicroserviceDTO", description = "Information about a microservice.")
public class MicroserviceDTO {

    @ApiModelProperty(value = "Main identifier of the microservice.", example = "1", position = 1)
    private Long id;
    @ApiModelProperty(value = "A name of the microservice.", example = "kypo2-training")
    private String name;
    @ApiModelProperty(value = "URI of the microservice.", example = "/kypo2-rest-training/api/v1")
    private String endpoint;

    /**
     * Gets the ID of the microservice.
     *
     * @return the ID of the microservice.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the microservice.
     *
     * @param id the ID of the microservice.
     */
    public void setId(Long id) {
        this.id = id;
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
     * Sets the name of the microservice.
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
     * Sets endpoint of the microservice.
     *
     * @param endpoint the endpoint of the microservice.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MicroserviceDTO)) return false;
        MicroserviceDTO that = (MicroserviceDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "MicroserviceDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
