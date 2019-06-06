package cz.muni.ics.kypo.userandgroup.api.dto;

import java.util.Objects;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class RoleAndMicroserviceDTO {

    private Long roleId;
    private Long microserviceId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(Long microserviceId) {
        this.microserviceId = microserviceId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoleAndMicroserviceDTO)) {
            return false;
        }
        RoleAndMicroserviceDTO other = (RoleAndMicroserviceDTO) object;
        return Objects.equals(getRoleId(), other.getRoleId()) &&
                Objects.equals(getMicroserviceId(), other.getMicroserviceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleId(), getMicroserviceId());
    }

    @Override
    public String toString() {
        return "RolesAndMicroservicesDTO{" +
                "roleId=" + roleId +
                ", microserviceId=" + microserviceId +
                '}';
    }
}
