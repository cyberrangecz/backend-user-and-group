package cz.cyberrange.platform.userandgroup.definition.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that completing a request would conflict with the current state of an entity.
 * CustomRestExceptionHandler answers it with HTTP 409 and an ApiEntityError body: the message
 * carries the entity detail's reason when a detail was given, and the fixed conflict text
 * otherwise; the single error entry carries the exception's own message; entityErrorDetail carries
 * the detail passed at construction, staying null when none was given.
 */
@ResponseStatus(
    value = HttpStatus.CONFLICT,
    reason =
        "The request could not be completed due to a conflict with the current state of the target resource.")
public class EntityConflictException extends ExceptionWithEntity {

  public EntityConflictException() {
    super();
  }

  public EntityConflictException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public EntityConflictException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public EntityConflictException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds the reason text used when the entity detail carries none of its own: the entity's class
   * name, followed by its identifier and value in parentheses when both are present.
   *
   * @param entityErrorDetail detail describing the conflicting entity
   * @return the assembled reason text
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason =
        new StringBuilder("Conflict with the current state of the target entity ")
            .append(entityErrorDetail.getEntity());
    if (entityErrorDetail.getIdentifier() != null
        && entityErrorDetail.getIdentifierValue() != null) {
      reason
          .append(" (")
          .append(entityErrorDetail.getIdentifier())
          .append(": ")
          .append(entityErrorDetail.getIdentifierValue())
          .append(")");
    }
    reason.append(".");
    return reason.toString();
  }
}
