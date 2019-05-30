package cz.muni.ics.kypo.userandgroup.security.exception;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SecurityException() {
        super();
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SecurityException(Throwable throwable) {
        super(throwable);
    }
}
