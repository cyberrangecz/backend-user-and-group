package cz.cyberrange.platform.userandgroup.definition.exceptions.errors;

import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * Error response body for a failure tied to one entity, extending ApiError with that entity's
 * detail. The message resolves to the entity detail's reason when it has one, falling back to the
 * caller-supplied message otherwise. entityErrorDetail carries the detail passed to the factory
 * method, and is null when the caller passes none.
 */
@Schema(
    name = "ApiEntityError",
    description = "Error body for a failure tied to one entity, with that entity's detail.")
public class ApiEntityError extends ApiError {
  private EntityErrorDetail entityErrorDetail;

  private ApiEntityError() {
    super();
  }

  /**
   * Builds an entity error response body, timestamped to the moment of the call. The message
   * carries the entity detail's reason when it has one, and the given message otherwise.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error used when the entity detail carries no reason
   * @param errors list of individual error reasons
   * @param path request path carried in the body
   * @param entityErrorDetail detail describing the entity involved in the error
   * @return the assembled error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      List<String> errors,
      String path,
      EntityErrorDetail entityErrorDetail) {
    ApiEntityError apiEntityError = new ApiEntityError();
    apiEntityError.setTimestamp(System.currentTimeMillis());
    apiEntityError.setStatus(httpStatus);
    apiEntityError.setMessage(getMessage(entityErrorDetail, message));
    apiEntityError.setErrors(errors);
    apiEntityError.setPath(path);
    apiEntityError.setEntityErrorDetail(entityErrorDetail);
    return apiEntityError;
  }

  /**
   * Builds an entity error response body carrying a single error reason, timestamped to the moment
   * of the call. The message carries the entity detail's reason when it has one, and the given
   * message otherwise.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error used when the entity detail carries no reason
   * @param error the single error reason, wrapped into a one-element list
   * @param path request path carried in the body
   * @param entityErrorDetail detail describing the entity involved in the error
   * @return the assembled error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      String error,
      String path,
      EntityErrorDetail entityErrorDetail) {
    ApiEntityError apiEntityError = new ApiEntityError();
    apiEntityError.setTimestamp(System.currentTimeMillis());
    apiEntityError.setStatus(httpStatus);
    apiEntityError.setMessage(getMessage(entityErrorDetail, message));
    apiEntityError.setError(error);
    apiEntityError.setPath(path);
    apiEntityError.setEntityErrorDetail(entityErrorDetail);
    return apiEntityError;
  }

  /**
   * Builds an entity error response body with an empty path, timestamped to the moment of the call.
   * The message carries the entity detail's reason when it has one, and the given message
   * otherwise.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error used when the entity detail carries no reason
   * @param errors list of individual error reasons
   * @param entityErrorDetail detail describing the entity involved in the error
   * @return the assembled error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      List<String> errors,
      EntityErrorDetail entityErrorDetail) {
    return ApiEntityError.of(httpStatus, message, errors, "", entityErrorDetail);
  }

  /**
   * Builds an entity error response body carrying a single error reason with an empty path,
   * timestamped to the moment of the call. The message carries the entity detail's reason when it
   * has one, and the given message otherwise.
   *
   * @param httpStatus status carried in the body
   * @param message description of the error used when the entity detail carries no reason
   * @param error the single error reason, wrapped into a one-element list
   * @param entityErrorDetail detail describing the entity involved in the error
   * @return the assembled error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus, String message, String error, EntityErrorDetail entityErrorDetail) {
    return ApiEntityError.of(httpStatus, message, error, "", entityErrorDetail);
  }

  // Falls back to defaultMessage when entityErrorDetail is null or carries no reason of its own.
  private static String getMessage(EntityErrorDetail entityErrorDetail, String defaultMessage) {
    if (entityErrorDetail == null) {
      return defaultMessage;
    }
    return entityErrorDetail.getReason() == null ? defaultMessage : entityErrorDetail.getReason();
  }

  public EntityErrorDetail getEntityErrorDetail() {
    return entityErrorDetail;
  }

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
    return "ApiEntityError{"
        + "entityErrorDetail="
        + entityErrorDetail
        + ", timestamp="
        + getTimestamp()
        + ", status="
        + getStatus()
        + ", message='"
        + getMessage()
        + '\''
        + ", errors="
        + getErrors()
        + ", path='"
        + getPath()
        + '\''
        + '}';
  }
}
