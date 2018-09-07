package cz.muni.ics.kypo.userandgroup.exception;

public class RoleCannotBeRemovedToGroupException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RoleCannotBeRemovedToGroupException() {
        super();
    }

    public RoleCannotBeRemovedToGroupException(String message) {
        super(message);
    }

    public RoleCannotBeRemovedToGroupException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RoleCannotBeRemovedToGroupException(Throwable throwable) {
        super(throwable);
    }
}
