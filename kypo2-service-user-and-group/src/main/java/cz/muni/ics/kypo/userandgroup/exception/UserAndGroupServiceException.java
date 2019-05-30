package cz.muni.ics.kypo.userandgroup.exception;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class UserAndGroupServiceException extends RuntimeException {

    public UserAndGroupServiceException() {
        super();
    }

    public UserAndGroupServiceException(String message) {
        super(message);
    }

    public UserAndGroupServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UserAndGroupServiceException(Throwable throwable) {
        super(throwable);
    }
}
