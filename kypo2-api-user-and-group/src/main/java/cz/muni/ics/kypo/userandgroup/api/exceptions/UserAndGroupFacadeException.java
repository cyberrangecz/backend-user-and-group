package cz.muni.ics.kypo.userandgroup.api.exceptions;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class UserAndGroupFacadeException extends RuntimeException {

    public UserAndGroupFacadeException() {
        super();
    }

    public UserAndGroupFacadeException(String message) {
        super(message);
    }

    public UserAndGroupFacadeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UserAndGroupFacadeException(Throwable throwable) {
        super(throwable);
    }
}
