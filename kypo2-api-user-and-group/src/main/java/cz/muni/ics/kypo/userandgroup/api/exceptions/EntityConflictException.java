package cz.muni.ics.kypo.userandgroup.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "The request could not be completed due to a conflict with the current state of the target resource.")
public class EntityConflictException extends ExceptionWithEntity {

    public EntityConflictException() {
        super();
    }

    public EntityConflictException(EntityErrorDetail entityErrorDetail) {
        super(entityErrorDetail);
    }

    public EntityConflictException(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(entityErrorDetail, cause);
    }

    public EntityConflictException(Throwable cause) {
        super(cause);
    }
}
