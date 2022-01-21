package cz.muni.ics.kypo.userandgroup.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        //it allows to use ResponseEntityExceptionHandler (ControllerAdvice) even for exceptions from the filters
        handlerExceptionResolver.resolveException(request, response, null, authException);
    }
}
