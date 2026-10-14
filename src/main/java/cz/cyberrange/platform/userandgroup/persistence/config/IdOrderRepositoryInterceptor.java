package cz.cyberrange.platform.userandgroup.persistence.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository method interceptor that gives every {@link Pageable} argument a total order, so pages
 * never overlap or skip rows when the requested sort key has ties.
 */
final class IdOrderRepositoryInterceptor implements MethodInterceptor {

  private static final String ID_PROPERTY = "id";

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Object[] arguments = invocation.getArguments();
    for (int index = 0; index < arguments.length; index++) {
      if (arguments[index] instanceof Pageable pageable) {
        arguments[index] = withIdOrder(pageable);
      }
    }
    return invocation.proceed();
  }

  /**
   * Appends ascending {@code id} to the sort of the given page request unless it already sorts by
   * {@code id}.
   *
   * @param pageable page request whose sort is completed
   * @return the same page request carrying the completed sort
   */
  static Pageable withIdOrder(Pageable pageable) {
    Sort completedSort = completeWithId(pageable.getSort());
    return pageable.isPaged()
        ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), completedSort)
        : Pageable.unpaged(completedSort);
  }

  private static Sort completeWithId(Sort sort) {
    return sort.getOrderFor(ID_PROPERTY) == null ? sort.and(Sort.by(ID_PROPERTY)) : sort;
  }
}
