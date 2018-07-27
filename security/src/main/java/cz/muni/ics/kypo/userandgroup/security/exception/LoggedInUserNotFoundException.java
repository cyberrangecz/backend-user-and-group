package cz.muni.ics.kypo.userandgroup.security.exception;

import org.springframework.security.core.AuthenticationException;

public class LoggedInUserNotFoundException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public LoggedInUserNotFoundException(String message) {
        super(message);
    }

    public LoggedInUserNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
