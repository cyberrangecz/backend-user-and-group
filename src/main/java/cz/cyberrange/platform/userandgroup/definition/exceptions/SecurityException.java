package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Signals that the current user is not authorized to access the requested resource.
 * CustomRestExceptionHandler has no dedicated handler for it, so it falls to the catch-all
 * Exception handler and is answered with HTTP 500 and an ApiError body whose message carries the
 * localized message of the deepest cause and whose single error entry carries the exception's own
 * message.
 */
public class SecurityException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SecurityException() {
    super();
  }

  public SecurityException(String message) {
    super(message);
  }

  public SecurityException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public SecurityException(Throwable throwable) {
    super(throwable);
  }
}
