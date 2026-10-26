package cz.cyberrange.platform.userandgroup.rest.sorting;

import java.util.Arrays;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Rejects a call whose {@link Pageable} arguments sort by a property that the {@link
 * SortableProperties} annotation of the REST handler serving the current request does not list,
 * throwing an {@link ErrorResponseException} that answers HTTP 400 with a {@link ProblemDetail}
 * body. A call outside a request, or from a handler without the annotation, proceeds unchecked.
 */
final class SortablePropertiesInterceptor implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    SortableProperties sortableProperties = currentHandlerSortableProperties();
    if (sortableProperties != null) {
      List<String> allowedProperties = sortableProperties.value().properties();
      Arrays.stream(invocation.getArguments())
          .filter(Pageable.class::isInstance)
          .map(Pageable.class::cast)
          .flatMap(pageable -> pageable.getSort().stream())
          .map(Sort.Order::getProperty)
          .filter(property -> !allowedProperties.contains(property))
          .findFirst()
          .ifPresent(
              property -> {
                throw unsortableProperty(property, allowedProperties);
              });
    }
    return invocation.proceed();
  }

  private static SortableProperties currentHandlerSortableProperties() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null
        && requestAttributes.getAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
            instanceof HandlerMethod handlerMethod) {
      return handlerMethod.getMethodAnnotation(SortableProperties.class);
    }
    return null;
  }

  private static ErrorResponseException unsortableProperty(
      String property, List<String> allowedProperties) {
    String sortedAllowedProperties =
        String.join(", ", allowedProperties.stream().sorted().toList());
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Cannot sort by '%s'. Sortable properties: %s."
                .formatted(property, sortedAllowedProperties));
    return new ErrorResponseException(HttpStatus.BAD_REQUEST, problemDetail, null);
  }
}
