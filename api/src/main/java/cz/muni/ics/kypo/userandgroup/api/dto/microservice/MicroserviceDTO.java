package cz.muni.ics.kypo.userandgroup.api.dto.microservice;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class MicroserviceDTO {

    private Long id;
    private String name;
    private String endpoint;

    @ApiModelProperty(value = "Main identifier of the microservice.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the microservice.", example = "kypo2-training")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "URI of the microservice.", example = "/kypo2-rest-training/api/v1")
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MicroserviceDTO that = (MicroserviceDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, endpoint);
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
