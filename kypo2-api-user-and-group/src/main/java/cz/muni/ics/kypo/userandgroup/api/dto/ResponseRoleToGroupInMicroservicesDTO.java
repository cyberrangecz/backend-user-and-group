package cz.muni.ics.kypo.userandgroup.api.dto;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.AssignRoleToGroupStatusDTO;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
    public boolean equals(Object object) {
        if (!(object instanceof ResponseRoleToGroupInMicroservicesDTO)) {
            return false;
        }
        ResponseRoleToGroupInMicroservicesDTO that = (ResponseRoleToGroupInMicroservicesDTO) object;
        return Objects.equals(getRoleId(), that.getRoleId()) &&
                Objects.equals(getMicroserviceId(), that.getMicroserviceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleId(), getMicroserviceId());
    }
}
