package cz.muni.ics.kypo.userandgroup.exception;

public class UserAndGroupFacadeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

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
