package cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extending of the class {@link Transactional} which has <i>read-only</i> set to true.
 */
@Transactional(rollbackFor = Exception.class, readOnly = true)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalRO {

    /**
     * Alias for <strong>transactionManager()<strong/>.
     */
    @AliasFor("transactionManager")
    String value() default "";

    /**
     * A <i>qualifier<i/> value for the specified transaction.
     */
    @AliasFor("value")
    String transactionManager() default "";

    /**
     * The transaction propagation type.
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * The transaction isolation type.
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * The timeout for this transaction (in seconds).
     */
    int timeout() default -1;

}
