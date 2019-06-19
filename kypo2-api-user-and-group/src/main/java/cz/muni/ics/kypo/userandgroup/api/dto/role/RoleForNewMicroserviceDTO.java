package cz.muni.ics.kypo.userandgroup.api.dto.role;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Encapsulates information about a role which will be used in new microservice.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class RoleForNewMicroserviceDTO {

    @NotEmpty(message = "{roleForNewMicroserviceDto.roleType.NotEmpty.message}")
    private String roleType;
    @NotNull(message = "{roleForNewMicroserviceDto.isDefault.NotNull.message}")
    private boolean isDefault;
    private String description;

    /**
     * Gets role type of the role.
     *
     * @return the role type of the role.
     */
    @ApiModelProperty(value = "Role type of role.", required = true, example = "ROLE_USER_AND_GROUP_ADMINISTRATOR")
    public String getRoleType() {
        return roleType;
    }

    /**
     * Sets role type of the role.
     *
     * @param roleType the role type of the role.
     */
    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    /**
     * Mark if the role is default.
     *
     * @return true if the role is default, false otherwise.
     */
    @ApiModelProperty(value = "Sign if role is default or not.", required = true, example = "true")
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Sets mark if the role is default.
     *
     * @param aDefault true if role is default, false otherwise.
     */
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    /**
     * Gets description of the role.
     *
     * @return the description of the role.
     */
    @ApiModelProperty(value = "A description of what the user is capable of with this role.", example = "This role will allow you to create and delete groups.")
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of the role.
     *
     * @param description the description of the role.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoleForNewMicroserviceDTO)) return false;
        RoleForNewMicroserviceDTO that = (RoleForNewMicroserviceDTO) object;
        return isDefault() == that.isDefault() &&
                Objects.equals(getRoleType(), that.getRoleType()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleType(), isDefault(), getDescription());
    }

    @Override
    public String toString() {
        return "RoleForNewMicroserviceDTO{" +
                "roleType='" + roleType + '\'' +
                ", isDefault=" + isDefault +
                ", description='" + description + '\'' +
                '}';
    }
}
