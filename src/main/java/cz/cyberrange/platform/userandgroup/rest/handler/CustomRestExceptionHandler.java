package cz.cyberrange.platform.userandgroup.rest.handler;

import cz.cyberrange.platform.userandgroup.definition.exceptions.BadRequestException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.FileCannotReadException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.FileNotFoundException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UagAccessForbiddenException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.UrlPathHelper;

/**
 * Central handler that turns exceptions raised while serving a REST request into ApiError or
 * ApiEntityError response bodies with a matching HTTP status.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
  private static final Logger LOG = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

  /**
   * Catches FileNotFoundException. Answers with HTTP 404 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({FileNotFoundException.class})
  public ResponseEntity<Object> handleFileNotFoundException(
      final FileNotFoundException ex, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.NOT_FOUND,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches FileCannotReadException. Answers with HTTP 500 and an ApiError whose message carries
   * the localized message of the deepest cause, whose single error entry carries the exception's
   * own message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({FileCannotReadException.class})
  public ResponseEntity<Object> handleFileCannotReadException(
      final FileCannotReadException ex, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches TypeMismatchException, including MethodArgumentTypeMismatchException. Ignores the
   * headers and status supplied by the base handler and always answers with HTTP 400 and an
   * ApiError whose message carries the localized message of the deepest cause, whose single error
   * entry carries the exception's own message, and whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      final TypeMismatchException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches MissingServletRequestPartException. Ignores the headers and status supplied by the base
   * handler and always answers with HTTP 400 and an ApiError whose message carries the localized
   * message of the deepest cause, whose single error entry carries the exception's own message, and
   * whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      final MissingServletRequestPartException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches MissingServletRequestParameterException. Ignores the headers and status supplied by the
   * base handler and always answers with HTTP 400 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      final MissingServletRequestParameterException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches NoHandlerFoundException. Ignores the headers and status supplied by the base handler
   * and always answers with HTTP 404 and an ApiError whose message carries the localized message of
   * the deepest cause, whose single error entry carries the exception's own message, and whose path
   * carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      final NoHandlerFoundException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.NOT_FOUND,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches HttpRequestMethodNotSupportedException. Ignores the headers and status supplied by the
   * base handler and always answers with HTTP 404 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the rejected method
   * followed by the list of supported methods, and whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      final HttpRequestMethodNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final StringBuilder supportedHttpMethods = new StringBuilder();
    supportedHttpMethods.append(ex.getMethod());
    supportedHttpMethods.append(
        " method is not supported for this request. Supported methods are ");
    ex.getSupportedHttpMethods().forEach(t -> supportedHttpMethods.append(t + " "));

    final ApiError apiError =
        ApiError.of(
            HttpStatus.NOT_FOUND,
            getInitialException(ex).getLocalizedMessage(),
            supportedHttpMethods.toString(),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches HttpMediaTypeNotSupportedException. Ignores the headers and status supplied by the base
   * handler and always answers with HTTP 415 and an ApiError whose message carries the localized
   * message of the deepest cause, whose single error entry carries the rejected media type followed
   * by the list of supported media types, and whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      final HttpMediaTypeNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final StringBuilder supportedMediaTypes = new StringBuilder();
    supportedMediaTypes.append(ex.getContentType());
    supportedMediaTypes.append(" media type is not supported. Supported media types are ");
    ex.getSupportedMediaTypes().forEach(t -> supportedMediaTypes.append(t + " "));

    final ApiError apiError =
        ApiError.of(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            getInitialException(ex).getLocalizedMessage(),
            supportedMediaTypes.toString(),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches MethodArgumentNotValidException. Ignores the headers and status supplied by the base
   * handler and always answers with HTTP 400 and an ApiError whose message carries the field
   * errors' default messages joined by ", " (ignoring any error not tied to a field), whose single
   * error entry carries the exception's own message, and whose path carries the request's context
   * path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(java.util.stream.Collectors.joining(", ")),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches HttpMessageNotReadableException. Ignores the headers and status supplied by the base
   * handler and always answers with HTTP 400 and an ApiError whose message carries the message of
   * the exception's most specific cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's context path.
   *
   * @param ex the caught exception
   * @param headers ignored
   * @param status ignored
   * @param request the failed request
   * @return the built error response
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            ex.getMostSpecificCause().getMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // Handling of own exceptions

  /**
   * Catches InsufficientAuthenticationException, a subtype of AuthenticationException. Answers with
   * HTTP 401 and an ApiError whose message carries the exception's own message, whose single error
   * entry also carries the exception's own message, and whose path carries the request's context
   * path. The req parameter is not used to build the response.
   *
   * @param ex the caught exception
   * @param request the failed request
   * @param req unused
   * @return the built error response
   */
  @ExceptionHandler({InsufficientAuthenticationException.class})
  protected ResponseEntity<Object> handleAuthenticationException(
      final InsufficientAuthenticationException ex,
      final WebRequest request,
      HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage(),
            getErrorMessage(ex),
            request.getContextPath());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches ConstraintViolationException. Answers with HTTP 400 and an ApiError whose message
   * carries the localized message of the deepest cause, whose single error entry carries the
   * exception's own message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(
      final ConstraintViolationException ex, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches BadRequestException. Answers with the status from its @ResponseStatus annotation (HTTP
   * 400) and an ApiError whose message carries the localized message of the deepest cause, whose
   * single error entry carries the exception's own message, and whose path carries the request's
   * full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Object> handleBadRequestException(
      final BadRequestException ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            BadRequestException.class.getAnnotation(ResponseStatus.class).value(),
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches EntityNotFoundException. Answers with the status from its @ResponseStatus annotation
   * (HTTP 404) and an ApiEntityError whose message carries the exception's entity detail's reason
   * when it has one and the annotation's fixed reason text otherwise, whose single error entry
   * carries the exception's own message, whose path carries the request's full URI, and whose
   * entityErrorDetail carries the exception's entity detail.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({EntityNotFoundException.class})
  public ResponseEntity<Object> handleEntityNotFoundException(
      final EntityNotFoundException ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiEntityError.of(
            EntityNotFoundException.class.getAnnotation(ResponseStatus.class).value(),
            EntityNotFoundException.class.getAnnotation(ResponseStatus.class).reason(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req),
            ex.getEntityErrorDetail());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches AccessDeniedException. Answers with HTTP 403 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({AccessDeniedException.class})
  public ResponseEntity<Object> handleSpringAccessDeniedException(
      org.springframework.security.access.AccessDeniedException ex,
      WebRequest request,
      HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.FORBIDDEN,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches DataAccessException. Answers with HTTP 409 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(
      final DataAccessException ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.CONFLICT,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // thrown from SERVICE layer (nullpointers, illegal argument etc.)
  /**
   * Catches IllegalArgumentException. Answers with HTTP 406 and an ApiError whose message carries
   * the localized message of the deepest cause, whose single error entry carries the exception's
   * own message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(
      final IllegalArgumentException ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.NOT_ACCEPTABLE,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches NullPointerException. Answers with HTTP 400 and an ApiError whose message carries the
   * localized message of the deepest cause, whose single error entry carries the exception's own
   * message, and whose path carries the request's full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<Object> handleNullPointerException(
      final NullPointerException ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // thrown from REST controller

  /**
   * Catches EntityConflictException. Answers with the status from its @ResponseStatus annotation
   * (HTTP 409) and an ApiEntityError whose message carries the exception's entity detail's reason
   * when it has one and the annotation's fixed reason text otherwise, whose single error entry
   * carries the exception's own message, whose path carries the request's full URI, and whose
   * entityErrorDetail carries the exception's entity detail.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({EntityConflictException.class})
  public ResponseEntity<Object> handleEntityConflictException(
      final EntityConflictException ex, final WebRequest request, HttpServletRequest req) {
    final ApiEntityError apiError =
        ApiEntityError.of(
            EntityConflictException.class.getAnnotation(ResponseStatus.class).value(),
            EntityConflictException.class.getAnnotation(ResponseStatus.class).reason(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req),
            ex.getEntityErrorDetail());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches UnprocessableEntityException. Answers with the status from its @ResponseStatus
   * annotation (HTTP 422) and an ApiEntityError whose message carries the exception's entity
   * detail's reason when it has one and the annotation's fixed reason text otherwise, whose single
   * error entry carries the exception's own message, whose path carries the request's full URI, and
   * whose entityErrorDetail carries the exception's entity detail.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({UnprocessableEntityException.class})
  public ResponseEntity<Object> handleUnprocessableEntityException(
      final UnprocessableEntityException ex, final WebRequest request, HttpServletRequest req) {
    final ApiEntityError apiError =
        ApiEntityError.of(
            UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).value(),
            UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).reason(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req),
            ex.getEntityErrorDetail());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches UagAccessForbiddenException. Answers with the status from its @ResponseStatus
   * annotation (HTTP 403) and an ApiError whose message carries the localized message of the
   * deepest cause, whose single error entry carries the exception's own message, and whose path
   * carries the request's full URI.
   *
   * @param ex the caught exception
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({UagAccessForbiddenException.class})
  public ResponseEntity<Object> handleUagAccessForbiddenException(
      final UagAccessForbiddenException ex, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            UagAccessForbiddenException.class.getAnnotation(ResponseStatus.class).value(),
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  /**
   * Catches any exception not caught by a more specific handler in this class. Answers with HTTP
   * 500 and an ApiError whose message carries the localized message of the deepest cause, whose
   * single error entry carries the exception's own message, and whose path carries the request's
   * full URI.
   *
   * @param ex the caught exception
   * @param request unused
   * @param req the failed request
   * @return the built error response
   */
  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleAll(
      final Exception ex, final WebRequest request, HttpServletRequest req) {
    final ApiError apiError =
        ApiError.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            getInitialException(ex).getLocalizedMessage(),
            getErrorMessage(ex),
            URL_PATH_HELPER.getRequestUri(req));
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  private Exception getInitialException(Exception exception) {
    while (exception.getCause() != null) {
      exception = (Exception) exception.getCause();
    }
    return exception;
  }

  private String getErrorMessage(Exception exception) {
    try (StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw)) {
      exception.printStackTrace(pw);
      LOG.error(sw.toString());
      return exception.getMessage();
    } catch (IOException ex) {
      LOG.error("It was not possible to get the stack trace for that exception: ", ex);
    }
    return "It was not possible to get the stack trace for that exception.";
  }
}
