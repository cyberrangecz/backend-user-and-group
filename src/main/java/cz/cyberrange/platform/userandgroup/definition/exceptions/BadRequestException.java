package cz.cyberrange.platform.userandgroup.definition.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals a request that could not be processed because of a problem with the request itself.
 * CustomRestExceptionHandler answers it with HTTP 400 and an ApiError body whose message carries
 * the localized message of the deepest cause and whose single error entry carries the exception's
 * own message.
 */
@ResponseStatus(
    value = HttpStatus.BAD_REQUEST,
    reason =
        "The server cannot or will not process the request due to an apparent client error (e.g., malformed request syntax, size too large, invalid request message framing, or deceptive request routing).")
public class BadRequestException extends RuntimeException {

  public BadRequestException() {}

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadRequestException(Throwable cause) {
    super(cause);
  }

  public BadRequestException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
