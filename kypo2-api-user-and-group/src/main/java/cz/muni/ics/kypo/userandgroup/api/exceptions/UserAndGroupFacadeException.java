package cz.muni.ics.kypo.userandgroup.api.exceptions;

/**
 * Custom <i>RuntimeException</i> is thrown when UserAndGroupServiceException is caught on facade layer.
 *
 */
public class UserAndGroupFacadeException extends RuntimeException {

    /**
     * Instantiates a new UserAndGroupFacadeException with <i>null<i/> as its detail message.
     */
    public UserAndGroupFacadeException() {
        super();
    }

    /**
     * Instantiates a new UserAndGroupFacadeException with the specified detail message.
     *
     * @param message the message
     */
    public UserAndGroupFacadeException(String message) {
        super(message);
    }

    /**
     * Instantiates a new UserAndGroupFacadeException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public UserAndGroupFacadeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Instantiates a new UserAndGroupFacadeException with the specified cause and detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param throwable the throwable
     */
    public UserAndGroupFacadeException(Throwable throwable) {
        super(throwable);
    }
}
