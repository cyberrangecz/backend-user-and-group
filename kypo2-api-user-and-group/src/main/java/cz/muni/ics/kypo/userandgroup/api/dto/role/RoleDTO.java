package cz.muni.ics.kypo.userandgroup.api.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class RoleDTO {

    private Long id;
    @JsonProperty("role_type")
    private String roleType;
    private Long idOfMicroservice;
    private String nameOfMicroservice;

    @ApiModelProperty(value = "Main identifier of the role.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Role type of role.", example = "ROLE_USER_AND_GROUP_ADMINISTRATOR")
    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    @ApiModelProperty(value = "Id of microservice which use this role.", example = "5")
    public Long getIdOfMicroservice() {
        return idOfMicroservice;
    }

    public void setIdOfMicroservice(Long idOfMicroservice) {
        this.idOfMicroservice = idOfMicroservice;
    }

    @ApiModelProperty(value = "A name of microservice which use this role.", example = "kypo2-training")
    public String getNameOfMicroservice() {
        return nameOfMicroservice;
    }

    public void setNameOfMicroservice(String nameOfMicroservice) {
        this.nameOfMicroservice = nameOfMicroservice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleDTO roleDTO = (RoleDTO) o;
        return Objects.equals(getId(), roleDTO.getId()) &&
                Objects.equals(getRoleType(), roleDTO.getRoleType()) &&
                Objects.equals(getNameOfMicroservice(), roleDTO.getNameOfMicroservice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRoleType(), getNameOfMicroservice());
    }

    @Override
    public String toString() {
        return "RoleDTO{" +
                "id=" + id +
                ", roleType='" + roleType + '\'' +
                ", idOfMicroservice=" + idOfMicroservice +
                ", nameOfMicroservice='" + nameOfMicroservice + '\'' +
                '}';
    }
}
