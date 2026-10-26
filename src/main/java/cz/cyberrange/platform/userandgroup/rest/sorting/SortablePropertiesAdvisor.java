package cz.cyberrange.platform.userandgroup.rest.sorting;

import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Applies {@link SortablePropertiesInterceptor} to every facade method taking a {@link Pageable}.
 * It runs after method security and before transactions open.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SortablePropertiesAdvisor extends StaticMethodMatcherPointcutAdvisor {

  private static final String FACADE_PACKAGE = UserFacade.class.getPackageName();

  public SortablePropertiesAdvisor() {
    super(new SortablePropertiesInterceptor());
    setOrder(Ordered.LOWEST_PRECEDENCE - 1);
    setClassFilter(candidateClass -> FACADE_PACKAGE.equals(candidateClass.getPackageName()));
  }

  @Override
  public boolean matches(Method method, Class<?> targetClass) {
    return Arrays.asList(method.getParameterTypes()).contains(Pageable.class);
  }
}
