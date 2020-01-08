package cz.muni.ics.kypo.userandgroup.rest.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for logging that cross-cutting multiple layers of project.
 *
 */
@Aspect
@Component
public class LoggingAspect {
    /**
     * Advice executed before particular join point in data layer.
     *
     * @param joinPoint method executed in data layer.
     */
    @Before("cz.muni.ics.kypo.userandgroup.rest.aop.CommonJoinPointConfig.dataLayerExecutionLoggingDebug()")
    public void dataLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint);
    }

    /**
     * Advice executed before particular join point in service layer.
     *
     * @param joinPoint method executed in service layer.
     */
    @Before("cz.muni.ics.kypo.userandgroup.rest.aop.CommonJoinPointConfig.serviceLayerExecutionLoggingDebug()")
    public void serviceLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint);
    }

    /**
     * Advice executed before particular join point in facade layer.
     *
     * @param joinPoint method executed in facade layer.
     */
    @Before("cz.muni.ics.kypo.userandgroup.rest.aop.CommonJoinPointConfig.facadeLayerExecutionLoggingDebug()")
    public void facadeLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint);
    }

    /**
     * Advice executed before particular join point in rest layer.
     *
     * @param joinPoint method executed in rest layer.
     */
    @Before("cz.muni.ics.kypo.userandgroup.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingDebug()")
    public void restLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint);
    }

    /**
     * Advice executed after throwing exception in rest layer.
     *
     * @param joinPoint method executed in rest layer.
     */
    @Before("cz.muni.ics.kypo.userandgroup.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingError()")
    public void afterThrowingExceptionInRestLayer(JoinPoint joinPoint) {
        Exception exception = (Exception) joinPoint.getArgs()[0];
        LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName()).error( "", exception);
    }


    private void logJoinPoint(JoinPoint joinPoint) {
        StringBuilder builder = new StringBuilder();
        builder.append(joinPoint.getSignature().getName())
                .append("(");
        for(Object o : joinPoint.getArgs()) {
            if(o == null) {
                builder.append(o)
                        .append(",");
            } else {
                builder.append(o)
                        .append(",");
            }
        }
        builder.delete(builder.length()-1, builder.length());
        builder.append(")");

        LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName()).debug(builder.toString());
    }

}
