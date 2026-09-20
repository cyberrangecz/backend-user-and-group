package cz.cyberrange.platform.userandgroup.definition.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that a request is syntactically valid but its data cannot be processed.
 * CustomRestExceptionHandler answers it with HTTP 422 and an ApiEntityError body: the message
 * carries the entity detail's reason when a detail was given, and the fixed "cannot be processed"
 * text otherwise; the single error entry carries the exception's own message; entityErrorDetail
 * carries the detail passed at construction, staying null when none was given.
 */
@ResponseStatus(
    value = HttpStatus.UNPROCESSABLE_ENTITY,
    reason = "The requested data cannot be processed.")
public class UnprocessableEntityException extends ExceptionWithEntity {

  public UnprocessableEntityException() {
    super();
  }

  public UnprocessableEntityException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public UnprocessableEntityException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public UnprocessableEntityException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds the reason text used when the entity detail carries none of its own: the entity's class
   * name, followed by its identifier and value in parentheses when both are present, and the words
   * "not found".
   *
   * @param entityErrorDetail detail describing the entity that could not be processed
   * @return the assembled reason text
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason =
        new StringBuilder("Unable to be process entity ").append(entityErrorDetail.getEntity());
    if (entityErrorDetail.getIdentifier() != null
        && entityErrorDetail.getIdentifierValue() != null) {
      reason
          .append(" (")
          .append(entityErrorDetail.getIdentifier())
          .append(": ")
          .append(entityErrorDetail.getIdentifierValue())
          .append(")");
    }
    reason.append(" not found.");
    return reason.toString();
  }
}
