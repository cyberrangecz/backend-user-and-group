package cz.muni.ics.kypo.userandgroup.api.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulates information about the role.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class RoleDTO {

    private Long id;
    @JsonProperty("role_type")
    private String roleType;
    private Long idOfMicroservice;
    private String nameOfMicroservice;
    private String description;

    /**
     * Gets the ID of the role.
     *
     * @return the ID of the role.
     */
    @ApiModelProperty(value = "Main identifier of the role.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the role.
     *
     * @param id the ID of the role.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets role type of the role.
     *
     * @return the role type of the role.
     */
    @ApiModelProperty(value = "Role type of role.", example = "ROLE_USER_AND_GROUP_ADMINISTRATOR")
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
     * Gets the ID of the microservice in which is role used.
     *
     * @return the ID of microservice in which is role used.
     */
    @ApiModelProperty(value = "Id of microservice which use this role.", example = "5")
    public Long getIdOfMicroservice() {
        return idOfMicroservice;
    }

    /**
     * Sets the ID of the microservice in which is role used.
     *
     * @param idOfMicroservice the ID of microservice in which is role used.
     */
    public void setIdOfMicroservice(Long idOfMicroservice) {
        this.idOfMicroservice = idOfMicroservice;
    }

    /**
     * Gets the name of the microservice in which is role used..
     *
     * @return the name of microservice in which is role used.
     */
    @ApiModelProperty(value = "A name of microservice which use this role.", example = "kypo2-training")
    public String getNameOfMicroservice() {
        return nameOfMicroservice;
    }

    /**
     * Sets the name of the microservice in which is role used.
     *
     * @param nameOfMicroservice the name of microservice in which is role used.
     */
    public void setNameOfMicroservice(String nameOfMicroservice) {
        this.nameOfMicroservice = nameOfMicroservice;
    }

    /**
     * Gets description in which is role used.
     *
     * @return the description in which is role used.
     */
    @ApiModelProperty(value = "A description of what the user is capable of with this role.", example = "This role will allow you to create and delete groups.")
    public String getDescription() {
        return description;
    }

    /**
     * Sets description in which is role used.
     *
     * @param description the description in which is role used.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoleDTO)) return false;
        RoleDTO roleDTO = (RoleDTO) object;
        return Objects.equals(getId(), roleDTO.getId()) &&
                Objects.equals(getRoleType(), roleDTO.getRoleType()) &&
                Objects.equals(getIdOfMicroservice(), roleDTO.getIdOfMicroservice()) &&
                Objects.equals(getNameOfMicroservice(), roleDTO.getNameOfMicroservice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRoleType(), getIdOfMicroservice(), getNameOfMicroservice());
    }

    @Override
    public String toString() {
        return "RoleDTO{" +
                "id=" + id +
                ", roleType='" + roleType + '\'' +
                ", idOfMicroservice=" + idOfMicroservice +
                ", nameOfMicroservice='" + nameOfMicroservice + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
