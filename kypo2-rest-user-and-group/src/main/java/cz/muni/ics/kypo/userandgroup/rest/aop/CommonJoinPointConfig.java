package cz.muni.ics.kypo.userandgroup.rest.aop;

import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Pavel Seda (441048)
 * @author Dominik Pilar (445537)
 */
public class CommonJoinPointConfig {

    @Pointcut("execution(* cz.muni.ics.kypo.userandgroup.repository.*.*(..))")
    public void dataLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.userandgroup.service.impl.*.*(..))")
    public void serviceLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.userandgroup.facade.*.*(..))")
    public void facadeLayerExecutionLoggingDebug() {
    }

    @Pointcut("within(cz.muni.ics.kypo.userandgroup.rest.controllers.*)")
    public void restLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler.*(..))")
    public void restLayerExecutionLoggingError() {
    }
}
