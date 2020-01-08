package cz.muni.ics.kypo.userandgroup.exceptions;

/**
 * Custom <i>RuntimeException</i> which is thrown when some error appear in service layer of application.
 *
 */
public class UserAndGroupServiceException extends RuntimeException {

    /**
     * Instantiates a new UserAndGroupException with <i>null<i/> as its detail message.
     */
    public UserAndGroupServiceException() {
        super();
    }

    /**
     * Instantiates a new UserAndGroupException with the specified detail message.
     *
     * @param message the message
     */
    public UserAndGroupServiceException(String message) {
        super(message);
    }

    /**
     * Instantiates a new UserAndGroupException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public UserAndGroupServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Instantiates a new UserAndGroupException with the specified cause and detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param throwable the throwable
     */
    public UserAndGroupServiceException(Throwable throwable) {
        super(throwable);
    }
}
