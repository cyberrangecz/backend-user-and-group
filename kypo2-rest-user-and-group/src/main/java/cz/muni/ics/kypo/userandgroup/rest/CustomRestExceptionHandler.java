package cz.muni.ics.kypo.userandgroup.rest;

import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * That is the base implementation of @ControllerAdvice, which is used so that all the handlers can
 * be managed from a central location. Another approach is to associate one handler with a set of
 * Controllers See:
 * https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html
 * </p>
 *
 * @author Pavel Šeda
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRestExceptionHandler.class);
    protected static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    // 400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
                                                                  final WebRequest request) {
        LOG.debug("handleMethodArgumentNotValid({}, {}, {}, {})", ex, headers, status, request);

        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setErrors(errors).setPath(request.getContextPath()).build();

        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        LOG.debug("handleBindException({}, {}, {}, {})", ex, headers, status, request);

        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setErrors(errors).setPath(request.getContextPath()).build();
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status,
                                                        final WebRequest request) {
        LOG.debug("handleTypeMismatch({}, {}, {}, {})", ex, headers, status, request);

        final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type " + ex.getRequiredType();

        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers,
                                                                     final HttpStatus status, final WebRequest request) {
        LOG.debug("handleMissingServletRequestPart({}, {}, {}, {})", ex, headers, status, request);

        final String error = ex.getRequestPartName() + " part is missing";
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers,
                                                                          final HttpStatus status, final WebRequest request) {
        LOG.debug("handleMissingServletRequestParameter({}, {}, {}, {})", ex, headers, status, request);

        final String error = ex.getParameterName() + " parameter is missing";
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // 404
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status,
                                                                   final WebRequest request) {
        LOG.debug("handleNoHandlerFoundException({}, {}, {}, {})", ex, headers, status, request);

        final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.NOT_FOUND, ex.getLocalizedMessage()).setError(error).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // 405
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpRequestMethodNotSupportedException ex, final HttpHeaders headers,
                                                                         final HttpStatus status, final WebRequest request) {
        LOG.debug("handleHttpRequestMethodNotSupported({}, {}, {}, {})", ex, headers, status, request);

        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));

        final ApiError apiError = new ApiError.APIErrorBuilder(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage()).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // 415
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers,
                                                                     final HttpStatus status, final WebRequest request) {
        LOG.debug("handleHttpMediaTypeNotSupported({}, {}, {}, {})", ex, headers, status, request);

        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));

        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2)).setPath(request.getContextPath()).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // Custom methods for handling own exceptions

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Object> handleBadGatewayException(final BadGatewayException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleBadGatewayException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(BadGatewayException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(BadGatewayException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(final BadRequestException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleBadRequestException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(BadRequestException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(BadRequestException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(final ForbiddenException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleForbiddenException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(ForbiddenException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(ForbiddenException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity<Object> handleGatewayTimeoutException(final GatewayTimeoutException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleGatewayTimeoutException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(GatewayTimeoutException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(GatewayTimeoutException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(HTTPVersionNotSupportedException.class)
    public ResponseEntity<Object> handleHTTPVersionNotSupportedException(final HTTPVersionNotSupportedException ex, final WebRequest request,
                                                                         HttpServletRequest req) {
        LOG.debug("handleHTTPVersionNotSupportedException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(HTTPVersionNotSupportedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(HTTPVersionNotSupportedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InsufficientStorageException.class)
    public ResponseEntity<Object> handleInsufficientStorageException(final InsufficientStorageException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleInsufficientStorageException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(InsufficientStorageException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(InsufficientStorageException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> handleInternalServerErrorException(final InternalServerErrorException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleInternalServerErrorException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(InternalServerErrorException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(InternalServerErrorException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> handleInvalidParameterException(final InvalidParameterException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleInvalidParameterException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(InvalidParameterException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(InvalidParameterException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(LoopDetectedException.class)
    public ResponseEntity<Object> handleLoopDetectedException(final LoopDetectedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleLoopDetectedException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(LoopDetectedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(LoopDetectedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<Object> handleMethodNotAllowedException(final MethodNotAllowedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleMethodNotAllowedException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(MethodNotAllowedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(MethodNotAllowedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(MovedPermanentlyException.class)
    public ResponseEntity<Object> handleMovedPermanentlyException(final MovedPermanentlyException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleMovedPermanentlyException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(MovedPermanentlyException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(MovedPermanentlyException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NetworkAuthenticationRequiredException.class)
    public ResponseEntity<Object> handleNetworkAuthenticationRequiredException(final NetworkAuthenticationRequiredException ex, final WebRequest request,
                                                                               HttpServletRequest req) {
        LOG.debug("handleNetworkAuthenticationRequiredException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(NetworkAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(NetworkAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Object> handleNoContentException(final NoContentException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleNoContentException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(NoContentException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(NoContentException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NotExtendedException.class)
    public ResponseEntity<Object> handleNotExtendedException(final NotExtendedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleNotExtendedException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(NotExtendedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(NotExtendedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<Object> handleNotImplementedException(final NotImplementedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleNotImplementedException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(NotImplementedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(NotImplementedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<Object> handlePayloadTooLargeException(final PayloadTooLargeException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handlePayloadTooLargeException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(PayloadTooLargeException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(PayloadTooLargeException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ProxyAuthenticationRequiredException.class)
    public ResponseEntity<Object> handleProxyAuthenticationRequiredException(final ProxyAuthenticationRequiredException ex, final WebRequest request,
                                                                             HttpServletRequest req) {
        LOG.debug("handleProxyAuthenticationRequiredException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ProxyAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ProxyAuthenticationRequiredException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(RangeNotSatisfiableException.class)
    public ResponseEntity<Object> handleRangeNotSatisfiableException(final RangeNotSatisfiableException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleRangeNotSatisfiableException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(RangeNotSatisfiableException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(RangeNotSatisfiableException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(RequestTimeoutException.class)
    public ResponseEntity<Object> handleRequestTimeoutException(final RequestTimeoutException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleRequestTimeoutException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(RequestTimeoutException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(RequestTimeoutException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(ResourceAlreadyExistingException.class)
    public ResponseEntity<Object> handleResourceAlreadyExistingException(final ResourceAlreadyExistingException ex, final WebRequest request,
                                                                         HttpServletRequest req) {
        LOG.debug("handleResourceAlreadyExistingException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ResourceAlreadyExistingException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ResourceAlreadyExistingException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // resource not created exception
    @ExceptionHandler({ResourceNotCreatedException.class})
    public ResponseEntity<Object> handleResourceNotCreatedException(final ResourceNotCreatedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleResourceNotCreatedException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ResourceNotCreatedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ResourceNotCreatedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // handle resource not found exceptions e.g. ~/{id} which does not exists
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(final ResourceNotFoundException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleResourceNotFoundException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ResourceNotFoundException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ResourceNotFoundException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // resource not created exception
    @ExceptionHandler({ResourceNotModifiedException.class})
    public ResponseEntity<Object> handleResourceNotModifiedException(final ResourceNotModifiedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleResourceNotModifiedException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ResourceNotModifiedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ResourceNotModifiedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ServiceUnavailableException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(final ServiceUnavailableException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleServiceUnavailableException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ServiceUnavailableException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(ServiceUnavailableException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({TooManyRequestsException.class})
    public ResponseEntity<Object> handleTooManyRequestsException(final TooManyRequestsException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleTooManyRequestsException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(TooManyRequestsException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(TooManyRequestsException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(final UnauthorizedException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleUnauthorizedException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(UnauthorizedException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(UnauthorizedException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnprocessableEntityException.class})
    public ResponseEntity<Object> handleUnprocessableEntityException(final UnprocessableEntityException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleUnprocessableEntityException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(UnprocessableEntityException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({UnsupportedMediaTypeException.class})
    public ResponseEntity<Object> handleUnsupportedMediaTypeException(final UnsupportedMediaTypeException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleUnsupportedMediaTypeException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(UnsupportedMediaTypeException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(UnsupportedMediaTypeException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({URITooLongException.class})
    public ResponseEntity<Object> handleURITooLongException(final URITooLongException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleURITooLongException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(URITooLongException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                .setError(URITooLongException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({VariantAlsoNegotiatesException.class})
    public ResponseEntity<Object> handleVariantAlsoNegotiatesException(final VariantAlsoNegotiatesException ex, final WebRequest request,
                                                                       HttpServletRequest req) {
        LOG.debug("handleVariantAlsoNegotiatesException({}, {}, {})", ex, request, req);

        final ApiError apiError =
                new ApiError.APIErrorBuilder(VariantAlsoNegotiatesException.class.getAnnotation(ResponseStatus.class).value(), ex.getLocalizedMessage())
                        .setError(VariantAlsoNegotiatesException.class.getAnnotation(ResponseStatus.class).reason()).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // Existing Java Exceptions

    // access denied
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        LOG.debug("handleAccessDeniedException({}, {})", ex, request);

        return new ResponseEntity<Object>("Access denied message here", new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<Object> handleSpringAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request, HttpServletRequest req) {
        LOG.info("handleAccessDeniedException({}, {})", ex, request);

        final ApiError apiError = new ApiError.APIErrorBuilder(HttpStatus.FORBIDDEN, ex.getLocalizedMessage()).setError("Access denied")
                .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.status);
    }

    // handle illegal argument exceptions e.g. given payload is not valid against
    // draft-v4 schema
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleIllegalArgumentException({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(HttpStatus.NOT_ACCEPTABLE, ex.getLocalizedMessage()).setError("Illegal Argument")
                .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request,
                                                                   HttpServletRequest req) {
        LOG.debug("handleMethodArgumentTypeMismatch({}, {}, {})", ex, request, req);

        final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();
        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setError(error).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleConstraintViolation({}, {}, {})", ex, request, req);

        final List<String> errors = new ArrayList<String>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": " + violation.getMessage());
        }

        final ApiError apiError =
                new ApiError.APIErrorBuilder(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()).setErrors(errors).setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // 409 - Conflict
    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<Object> handleConflictException(final ConflictException ex, final WebRequest request,
                                                          HttpServletRequest req) {
        LOG.error("handleConflictException({}, {}, {})", new Object[]{ex, request, req});

        final ApiError apiError =
                new ApiError.APIErrorBuilder(ConflictException.class.getAnnotation(ResponseStatus.class).value(),
                        ex.getLocalizedMessage()).setError(ConflictException.class.getAnnotation(ResponseStatus.class).reason())
                        .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    // 500
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request, HttpServletRequest req) {
        LOG.debug("handleAll({}, {}, {})", ex, request, req);

        final ApiError apiError = new ApiError.APIErrorBuilder(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage()).setError("error occurred")
                .setPath(URL_PATH_HELPER.getRequestUri(req)).build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
