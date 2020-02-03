package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "The requested resource already exists")
public class ResourceAlreadyExistingException extends RuntimeException {

    public ResourceAlreadyExistingException() {
    }

    public ResourceAlreadyExistingException(String message) {
        super(message);
    }

    public ResourceAlreadyExistingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceAlreadyExistingException(Throwable cause) {
        super(cause);
    }

    public ResourceAlreadyExistingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
