package cz.muni.ics.kypo.userandgroup.exceptions;

/**
 * Custom RuntimeException is thrown when some error appears during loading roles and users.
 */
public class LoadingRolesAndUserException extends RuntimeException {

    /**
     * Instantiates a new LoadingRolesAndUserException with <i>null<i/> as its detail message.
     */
    public LoadingRolesAndUserException() {
        super();
    }

    /**
     * Instantiates a new LoadingRolesAndUserException with the specified detail message.
     *
     * @param message the message
     */
    public LoadingRolesAndUserException(String message) {
        super(message);
    }

    /**
     * Instantiates a new LoadingRolesAndUserException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public LoadingRolesAndUserException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Instantiates a new LoadingRolesAndUserException with the specified cause and detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param throwable the throwable
     */
    public LoadingRolesAndUserException(Throwable throwable) {
        super(throwable);
    }

}
