package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Signals that generating an icon failed. CustomRestExceptionHandler has no dedicated handler for
 * it, so it falls to the catch-all Exception handler and is answered with HTTP 500 and an ApiError
 * body whose message carries the localized message of the deepest cause and whose single error
 * entry carries the exception's own message.
 */
public class IconGenerationException extends RuntimeException {

  public IconGenerationException() {}

  public IconGenerationException(String message) {
    super(message);
  }

  public IconGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  public IconGenerationException(Throwable cause) {
    super(cause);
  }

  public IconGenerationException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
