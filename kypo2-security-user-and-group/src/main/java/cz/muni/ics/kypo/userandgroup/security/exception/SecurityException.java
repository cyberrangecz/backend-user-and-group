package cz.muni.ics.kypo.userandgroup.security.exception;

/**
 * Custom RuntimeException which is thrown when user is not authorized to get required resources
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new SecurityException with <i>null<i/> as its detail message.
     */
    public SecurityException() {
        super();
    }

    /**
     * Instantiates a new SecurityException with the specified detail message.
     *
     * @param message the message
     */
    public SecurityException(String message) {
        super(message);
    }

    /**
     * Instantiates a new SecurityException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public SecurityException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Instantiates a new SecurityException with the specified cause and detail message of
     *      * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause)..
     *
     * @param throwable the throwable
     */
    public SecurityException(Throwable throwable) {
        super(throwable);
    }
}
