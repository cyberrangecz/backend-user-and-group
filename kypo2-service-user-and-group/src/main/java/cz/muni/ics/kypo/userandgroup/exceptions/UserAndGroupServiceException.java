package cz.muni.ics.kypo.userandgroup.exceptions;

/**
 * Custom <i>RuntimeException</i> which is thrown when some error appear in service layer of application.
 */
public class UserAndGroupServiceException extends RuntimeException {
    private final ErrorCode code;

    /**
     * Instantiates a new UserAndGroupException with <i>null<i/> as its detail message.
     *
     * @param code the code specifies the type of error
     */
    public UserAndGroupServiceException(ErrorCode code) {
        super();
        this.code = code;
    }

    /**
     * Instantiates a new UserAndGroupException with the specified detail message.
     *
     * @param message the message
     * @param code the code specifies the type of error
     */
    public UserAndGroupServiceException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    /**
     * Instantiates a new UserAndGroupException with the specified detail message and cause.
     *
     * @param message   the message
     * @param throwable the throwable
     * @param code  the code specifies the type of error
     */
    public UserAndGroupServiceException(String message, Throwable throwable, ErrorCode code) {
        super(message, throwable);
        this.code = code;
    }

    /**
     * Instantiates a new UserAndGroupException with the specified cause and detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param throwable the throwable
     * @param code  the code specifies the type of error
     */
    public UserAndGroupServiceException(Throwable throwable, ErrorCode code) {
        super(throwable);
        this.code = code;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }
}
