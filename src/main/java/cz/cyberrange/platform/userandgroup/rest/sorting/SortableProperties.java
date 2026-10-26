package cz.cyberrange.platform.userandgroup.rest.sorting;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the entity whose properties a client may sort a paged endpoint by. A request sorting by any
 * other property is answered with HTTP 400, and the API documentation offers only these properties.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SortableProperties {

  /**
   * Returns the entity whose properties are sortable.
   *
   * @return the entity kind
   */
  SortableEntity value();
}
