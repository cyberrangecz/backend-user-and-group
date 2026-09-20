package cz.cyberrange.platform.userandgroup.definition.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that a requested entity does not exist. CustomRestExceptionHandler answers it with HTTP
 * 404 and an ApiEntityError body: the message carries the entity detail's reason when a detail was
 * given, and the fixed "could not be found" text otherwise; the single error entry carries the
 * exception's own message; entityErrorDetail carries the detail passed at construction, staying
 * null when none was given.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested entity could not be found")
public class EntityNotFoundException extends ExceptionWithEntity {
  public EntityNotFoundException() {
    super();
  }

  public EntityNotFoundException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public EntityNotFoundException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public EntityNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds the reason text used when the entity detail carries none of its own: the entity's class
   * name, followed by its identifier and value in parentheses when both are present, and the words
   * "not found".
   *
   * @param entityErrorDetail detail describing the missing entity
   * @return the assembled reason text
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason = new StringBuilder("Entity ").append(entityErrorDetail.getEntity());
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
