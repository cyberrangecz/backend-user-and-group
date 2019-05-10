package cz.muni.ics.kypo.userandgroup.api.dto.role;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class RoleForNewMicroserviceDTO {

    @NotEmpty(message = "{roleForNewMicroserviceDto.roleType.NotEmpty.message}")
    private String roleType;
    @NotNull(message = "{roleForNewMicroserviceDto.isDefault.NotNull.message}")
    private boolean isDefault;

    @ApiModelProperty(value = "Role type of role.", required = true, example = "ROLE_USER_AND_GROUP_ADMINISTRATOR")
    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    @ApiModelProperty(value = "Sign if role is default or not.", required = true, example = "true")
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleForNewMicroserviceDTO that = (RoleForNewMicroserviceDTO) o;
        return isDefault == that.isDefault &&
                roleType.equals(that.roleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleType, isDefault);
    }

    @Override
    public String toString() {
        return "RoleForNewMicroserviceDTO{" +
                "roleType='" + roleType + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
