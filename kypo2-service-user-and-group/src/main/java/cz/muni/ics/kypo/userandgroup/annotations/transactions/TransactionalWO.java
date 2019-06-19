package cz.muni.ics.kypo.userandgroup.annotations.transactions;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Extending of the class {@link Transactional} which has <i>read-only</i> set to false.
 *
 * @author Pavel Seda
 */
@Transactional(rollbackFor = Exception.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalWO {

    /**
     * Alias for <strong>transactionManager()<strong/>.
     *
     */
    @AliasFor("transactionManager")
    String value() default "";

    /**
     * A <i>qualifier<i/> value for the specified transaction.
     *
     */
    @AliasFor("value")
    String transactionManager() default "";

    /**
     * The transaction propagation type.
     *
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * The transaction isolation type.
     *
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * The timeout for this transaction (in seconds).
     *
     */
    int timeout() default -1;
}
