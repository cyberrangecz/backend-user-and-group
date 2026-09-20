package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Signals that a file exists but its contents could not be read. CustomRestExceptionHandler answers
 * it with HTTP 500 and an ApiError body whose message carries the localized message of the deepest
 * cause and whose single error entry carries the exception's own message.
 */
public class FileCannotReadException extends RuntimeException {

  public FileCannotReadException() {}

  public FileCannotReadException(String message) {
    super(message);
  }

  public FileCannotReadException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileCannotReadException(Throwable cause) {
    super(cause);
  }

  public FileCannotReadException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
