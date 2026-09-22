package cz.cyberrange.platform.userandgroup.definition.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that the current user is not authorized to access the requested resource.
 * CustomRestExceptionHandler answers it with HTTP 403 and an ApiError body whose message carries
 * the exception's own localized message and whose single error entry carries the exception's own
 * message.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UagAccessForbiddenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UagAccessForbiddenException() {
    super();
  }

  public UagAccessForbiddenException(String message) {
    super(message);
  }

  public UagAccessForbiddenException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public UagAccessForbiddenException(Throwable throwable) {
    super(throwable);
  }
}
