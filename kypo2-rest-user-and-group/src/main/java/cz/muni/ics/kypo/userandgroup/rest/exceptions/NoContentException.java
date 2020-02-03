package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "The server successfully processed the request and is not returning any content.")
public class NoContentException extends RuntimeException {

    public NoContentException() {
    }

    public NoContentException(String message) {
        super(message);
    }

    public NoContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoContentException(Throwable cause) {
        super(cause);
    }

    public NoContentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
