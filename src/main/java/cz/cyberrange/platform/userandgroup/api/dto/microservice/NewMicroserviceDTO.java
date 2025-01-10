package cz.cyberrange.platform.userandgroup.api.dto.microservice;

import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates information about new microservice to be created in the database.
 */
@ApiModel(value = "NewMicroserviceDTO", description = "Data that need to be provided to register a new microservice.")
public class NewMicroserviceDTO {

    @ApiModelProperty(value = "A name of the microservice.", required = true, example = "training")
    @NotEmpty(message = "{microservice.name.NotEmpty.message}")
    private String name;
    @ApiModelProperty(value = "URI of the microservice.", required = true, example = "/training/api/v1")
    @NotEmpty(message = "{microservice.endpoint.NotEmpty.message}")
    private String endpoint;
    @ApiModelProperty(value = "Roles which are used by the microservice.", required = true)
    @Valid
    @NotNull(message = "{microservice.roles.NotNull.message}")
    private Set<@NotNull RoleForNewMicroserviceDTO> roles;

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

    /**
     * Gets roles of the microservice.
     *
     * @return the {@link RoleForNewMicroserviceDTO} of the microservice.
     */
    public Set<RoleForNewMicroserviceDTO> getRoles() {
        return roles;
    }

    /**
     * Sets roles of the microservice.
     *
     * @param roles the {@link RoleForNewMicroserviceDTO} of the microservice.
     */
    public void setRoles(Set<RoleForNewMicroserviceDTO> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NewMicroserviceDTO)) return false;
        NewMicroserviceDTO that = (NewMicroserviceDTO) object;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
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
