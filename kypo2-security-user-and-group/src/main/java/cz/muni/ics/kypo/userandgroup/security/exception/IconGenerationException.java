package cz.muni.ics.kypo.userandgroup.security.exception;

public class IconGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IconGenerationException() {
        super();
    }

    public IconGenerationException(String message) {
        super(message);
    }

    public IconGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IconGenerationException(Throwable cause) {
        super(cause);
    }

    protected IconGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
