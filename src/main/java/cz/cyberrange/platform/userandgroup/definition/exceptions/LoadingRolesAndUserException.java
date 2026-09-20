package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Signals a failure while loading roles and users. CustomRestExceptionHandler has no dedicated
 * handler for it, so it falls to the catch-all Exception handler and is answered with HTTP 500 and
 * an ApiError body whose message carries the localized message of the deepest cause and whose
 * single error entry carries the exception's own message.
 */
public class LoadingRolesAndUserException extends RuntimeException {

  public LoadingRolesAndUserException() {
    super();
  }

  public LoadingRolesAndUserException(String message) {
    super(message);
  }

  public LoadingRolesAndUserException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public LoadingRolesAndUserException(Throwable throwable) {
    super(throwable);
  }
}
