package cz.muni.ics.kypo.userandgroup.api.dto;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleAndMicroserviceDTO that = (RoleAndMicroserviceDTO) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(microserviceId, that.microserviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, microserviceId);
    }

    @Override
    public String toString() {
        return "RolesAndMicroservicesDTO{" +
                "roleId=" + roleId +
                ", microserviceId=" + microserviceId +
                '}';
    }
}
