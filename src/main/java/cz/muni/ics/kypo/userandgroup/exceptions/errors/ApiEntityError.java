package cz.muni.ics.kypo.userandgroup.exceptions.errors;

import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

@ApiModel(value = "ApiEntityError", description = "A detailed error information related to the entity.", parent = ApiError.class)
public class ApiEntityError extends ApiError {
    @ApiModelProperty(value = "Entity detail related to the error.")
    private EntityErrorDetail entityErrorDetail;

    private ApiEntityError() {
        super();
    }

    public static ApiEntityError of(HttpStatus httpStatus, String message, List<String> errors, String path, EntityErrorDetail entityErrorDetail) {
        ApiEntityError apiEntityError = new ApiEntityError();
        apiEntityError.setTimestamp(System.currentTimeMillis());
        apiEntityError.setStatus(httpStatus);
        apiEntityError.setMessage(getMessage(entityErrorDetail, message));
        apiEntityError.setErrors(errors);
        apiEntityError.setPath(path);
        apiEntityError.setEntityErrorDetail(entityErrorDetail);
        return apiEntityError;
    }

    public static ApiEntityError of(HttpStatus httpStatus, String message, String error, String path, EntityErrorDetail entityErrorDetail) {
        ApiEntityError apiEntityError = new ApiEntityError();
        apiEntityError.setTimestamp(System.currentTimeMillis());
        apiEntityError.setStatus(httpStatus);
        apiEntityError.setMessage(getMessage(entityErrorDetail, message));
        apiEntityError.setError(error);
        apiEntityError.setPath(path);
        apiEntityError.setEntityErrorDetail(entityErrorDetail);
        return apiEntityError;
    }

    public static ApiEntityError of(HttpStatus httpStatus, String message, List<String> errors, EntityErrorDetail entityErrorDetail) {
        return ApiEntityError.of(httpStatus, message, errors, "", entityErrorDetail);
    }

    public static ApiEntityError of(HttpStatus httpStatus, String message, String error, EntityErrorDetail entityErrorDetail) {
        return ApiEntityError.of(httpStatus, message, error, "", entityErrorDetail);
    }

    private static String getMessage(EntityErrorDetail entityErrorDetail, String defaultMessage) {
        if (entityErrorDetail == null) {
            return defaultMessage;
        }
        return entityErrorDetail.getReason() == null ? defaultMessage : entityErrorDetail.getReason();
    }

    /**
     * Gets entity error detail.
     *
     * @return the entity error detail
     */
    public EntityErrorDetail getEntityErrorDetail() {
        return entityErrorDetail;
    }

    /**
     * Sets entity error detail.
     *
     * @param entityErrorDetail the entity error detail
     */
    public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
        this.entityErrorDetail = entityErrorDetail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiEntityError)) return false;
        if (!super.equals(o)) return false;
        ApiEntityError that = (ApiEntityError) o;
        return Objects.equals(getEntityErrorDetail(), that.getEntityErrorDetail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getEntityErrorDetail());
    }

    @Override
    public String toString() {
        return "ApiEntityError{" +
                "entityErrorDetail=" + entityErrorDetail +
                ", timestamp=" + getTimestamp() +
                ", status=" + getStatus() +
                ", message='" + getMessage() + '\'' +
                ", errors=" + getErrors() +
                ", path='" + getPath() + '\'' +
                '}';
    }
}
