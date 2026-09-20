package cz.cyberrange.platform.userandgroup.definition.exceptions.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * Error response body returned for a failed request. The timestamp and status always reflect the
 * moment and status of the failure. The message and errors fields carry whatever description and
 * reasons the caller supplies to a factory method, staying null when the source exception has no
 * message text of its own. The path field carries the caller-supplied request path, staying an
 * empty string on the overloads that accept none.
 */
public class ApiError {

  @Schema(
      description = "Milliseconds since the Unix epoch, taken when the error was built.",
      example = "1574062900000")
  protected long timestamp;

  @Schema(example = "NOT_FOUND")
  protected HttpStatus status;

  @Schema(example = "The IDMGroup could not be found in database.")
  protected String message;

  @Schema(example = "[\"The requested resource was not found.\"]")
  protected List<String> errors;

  @Schema(
      description = "Path of the failed request; empty when none was recorded.",
      example = "/user-and-group/api/v1/groups/1000")
  protected String path;

  protected ApiError() {}

  /**
   * Builds an error response body, timestamped to the moment of the call.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error
   * @param errors list of individual error reasons
   * @param path request path carried in the body
   * @return the assembled error body
   */
  public static ApiError of(
      HttpStatus httpStatus, String message, List<String> errors, String path) {
    ApiError apiError = new ApiError();
    apiError.setTimestamp(System.currentTimeMillis());
    apiError.setStatus(httpStatus);
    apiError.setMessage(message);
    apiError.setErrors(errors);
    apiError.setPath(path);
    return apiError;
  }

  /**
   * Builds an error response body carrying a single error reason, timestamped to the moment of the
   * call.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error
   * @param error the single error reason, wrapped into a one-element list
   * @param path request path carried in the body
   * @return the assembled error body
   */
  public static ApiError of(HttpStatus httpStatus, String message, String error, String path) {
    ApiError apiError = new ApiError();
    apiError.setTimestamp(System.currentTimeMillis());
    apiError.setStatus(httpStatus);
    apiError.setMessage(message);
    apiError.setError(error);
    apiError.setPath(path);
    return apiError;
  }

  /**
   * Builds an error response body with an empty path, timestamped to the moment of the call.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error
   * @param errors list of individual error reasons
   * @return the assembled error body
   */
  public static ApiError of(HttpStatus httpStatus, String message, List<String> errors) {
    ApiError apiError = new ApiError();
    apiError.setTimestamp(System.currentTimeMillis());
    apiError.setStatus(httpStatus);
    apiError.setMessage(message);
    apiError.setErrors(errors);
    apiError.setPath("");
    return apiError;
  }

  /**
   * Builds an error response body carrying a single error reason with an empty path, timestamped to
   * the moment of the call.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error
   * @param error the single error reason, wrapped into a one-element list
   * @return the assembled error body
   */
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

  /**
   * Replaces the errors list with a single-element list built from the given error.
   *
   * @param error the error to store
   */
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
    return getTimestamp() == apiError.getTimestamp()
        && getStatus() == apiError.getStatus()
        && Objects.equals(getMessage(), apiError.getMessage())
        && Objects.equals(getErrors(), apiError.getErrors())
        && Objects.equals(getPath(), apiError.getPath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTimestamp(), getStatus(), getMessage(), getErrors(), getPath());
  }

  @Override
  public String toString() {
    return "ApiError{"
        + "timestamp="
        + timestamp
        + ", status="
        + status
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", path='"
        + path
        + '\''
        + '}';
  }
}
