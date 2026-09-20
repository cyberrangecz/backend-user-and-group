package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Signals that a requested file could not be located. CustomRestExceptionHandler answers it with
 * HTTP 404 and an ApiError body whose message carries the localized message of the deepest cause
 * and whose single error entry carries the exception's own message.
 */
public class FileNotFoundException extends RuntimeException {

  public FileNotFoundException() {}

  public FileNotFoundException(String message) {
    super(message);
  }

  public FileNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileNotFoundException(Throwable cause) {
    super(cause);
  }

  public FileNotFoundException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
