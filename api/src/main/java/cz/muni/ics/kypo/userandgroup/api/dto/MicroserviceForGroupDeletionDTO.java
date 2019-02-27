package cz.muni.ics.kypo.userandgroup.api.dto;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public class MicroserviceForGroupDeletionDTO {

    private Long id;
    private String name;
    private HttpStatus httpStatus;
    private String responseMessage;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public String toString() {
        return "MicroserviceForGroupDeletionDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", httpStatus=" + httpStatus +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MicroserviceForGroupDeletionDTO)) return false;
        MicroserviceForGroupDeletionDTO that = (MicroserviceForGroupDeletionDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                httpStatus == that.httpStatus &&
                Objects.equals(responseMessage, that.responseMessage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, httpStatus, responseMessage);
    }
}
