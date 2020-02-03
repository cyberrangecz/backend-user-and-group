package cz.muni.ics.kypo.userandgroup.rest.exceptionhandling;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Represents a possible representation of errors to be used with the @ControllerAdvice global
 * exceptions handler.
 * </p>
 */
public class ApiError {

    @ApiModelProperty(value = "The time when the exception occurred", example = "1574062900 (different for each type of exception)")
    protected long timestamp;
    @ApiModelProperty(value = "The HTTP response status code", example = "404 Not found (different for each type of exception).")
    protected HttpStatus status;
    @ApiModelProperty(value = "The specific description of the ApiError.", example = "The IDMGroup could not be found in database (different for each type of exception).")
    protected String message;
    @ApiModelProperty(value = "The list of main reasons of the ApiError.", example = "[\"The requested resource was not found (different for each type of exception).\"]")
    protected List<String> errors;
    @ApiModelProperty(value = "The requested URI path which caused error.", example = "/kypo2-rest-user-and-group/api/v1/groups/1000 (different for each type of exception).")
    protected String path;

    private ApiError() {
    }

    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors, String path) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath(path);
        return apiError;
    }

    public static ApiError of(HttpStatus httpStatus, String message, String error, String path) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath(path);
        return apiError;
    }

    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath("");
        return apiError;
    }

    public static ApiError of(HttpStatus httpStatus, String message, String error) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath("");
        return apiError;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(final HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }

    public void setError(final String error) {
        errors = Arrays.asList(error);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiError)) return false;
        ApiError apiError = (ApiError) o;
        return getTimestamp() == apiError.getTimestamp() &&
                getStatus() == apiError.getStatus() &&
                Objects.equals(getMessage(), apiError.getMessage()) &&
                Objects.equals(getErrors(), apiError.getErrors()) &&
                Objects.equals(getPath(), apiError.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimestamp(), getStatus(), getMessage(), getErrors(), getPath());
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                ", path='" + path + '\'' +
                '}';
    }
}
