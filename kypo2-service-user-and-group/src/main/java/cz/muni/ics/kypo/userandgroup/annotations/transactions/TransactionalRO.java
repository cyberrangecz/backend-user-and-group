package cz.muni.ics.kypo.userandgroup.annotations.transactions;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author Pavel Seda (441048)
 */
@Transactional(rollbackFor = Exception.class, readOnly = true)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalRO {
}
