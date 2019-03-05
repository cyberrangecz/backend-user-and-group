package cz.muni.ics.kypo.userandgroup.api.dto;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.AssignRoleToGroupStatusDTO;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ResponseRoleToGroupInMicroservicesDTO {
    private Long roleId;
    private Long microserviceId;
    private AssignRoleToGroupStatusDTO status;
    private String responseMessage;

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

    public AssignRoleToGroupStatusDTO getStatus() {
        return status;
    }

    public void setStatus(AssignRoleToGroupStatusDTO status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public String toString() {
        return "ResponseRoleToGroupInMicroservicesDTO{" +
                "roleId=" + roleId +
                ", microserviceId=" + microserviceId +
                ", status=" + status +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseRoleToGroupInMicroservicesDTO that = (ResponseRoleToGroupInMicroservicesDTO) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(microserviceId, that.microserviceId) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, microserviceId, status);
    }
}
