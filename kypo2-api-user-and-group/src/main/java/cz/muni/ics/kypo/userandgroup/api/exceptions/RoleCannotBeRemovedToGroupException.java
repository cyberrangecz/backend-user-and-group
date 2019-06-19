package cz.muni.ics.kypo.userandgroup.api.exceptions;

/**
 * Custom <i>RuntimeException</i> is thrown when a role cannot be removed from a group.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class RoleCannotBeRemovedToGroupException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new RoleCannotBeRemovedToGroupException with <i>null<i/> as its detail message.
     */
    public RoleCannotBeRemovedToGroupException() {
        super();
    }

    /**
     * Instantiates a new RoleCannotBeRemovedToGroupException with the specified detail message.
     *
     * @param message the message
     */
    public RoleCannotBeRemovedToGroupException(String message) {
        super(message);
    }

    /**
     * Instantiates a new RoleCannotBeRemovedToGroupException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public RoleCannotBeRemovedToGroupException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Instantiates a new RoleCannotBeRemovedToGroupException with the specified cause and detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param throwable the throwable
     */
    public RoleCannotBeRemovedToGroupException(Throwable throwable) {
        super(throwable);
    }
}
