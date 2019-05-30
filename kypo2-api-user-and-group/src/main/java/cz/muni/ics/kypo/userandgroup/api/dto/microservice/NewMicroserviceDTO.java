package cz.muni.ics.kypo.userandgroup.api.dto.microservice;

import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class NewMicroserviceDTO {

    @NotEmpty(message = "{newMicroserviceDto.name.NotEmpty.message}")
    private String name;
    @NotEmpty(message = "{newMicroserviceDto.endpoint.NotEmpty.message}")
    private String endpoint;
    @Valid
    @NotNull(message = "{newMicroserviceDto.roles.NotNull.message}")
    private Set<RoleForNewMicroserviceDTO> roles;

    @ApiModelProperty(value = "A name of the microservice.", required = true, example = "kypo2-training")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "URI of the microservice.", required = true, example = "/kypo2-rest-training/api/v1")
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @ApiModelProperty(value = "Roles which are used by the microservice.", required = true)
    public Set<RoleForNewMicroserviceDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleForNewMicroserviceDTO> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewMicroserviceDTO that = (NewMicroserviceDTO) o;
        return name.equals(that.name) &&
                endpoint.equals(that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, endpoint);
    }

    @Override
    public String toString() {
        return "NewMicroserviceDTO{" +
                "name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", roles=" + roles +
                '}';
    }
}
