package cz.muni.ics.kypo.userandgroup.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "The requested data cannot be processed.")
public class UnprocessableEntityException extends ExceptionWithEntity {

    public UnprocessableEntityException() {
        super();
    }

    public UnprocessableEntityException(EntityErrorDetail entityErrorDetail) {
        super(entityErrorDetail);
    }

    public UnprocessableEntityException(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(entityErrorDetail, cause);
    }

    public UnprocessableEntityException(Throwable cause) {
        super(cause);
    }

    protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
        StringBuilder reason = new StringBuilder("Unable to be process entity ")
                .append(entityErrorDetail.getEntity());
        if (entityErrorDetail.getIdentifier() != null && entityErrorDetail.getIdentifierValue() != null) {
            reason.append(" (")
                    .append(entityErrorDetail.getIdentifier())
                    .append(": ")
                    .append(entityErrorDetail.getIdentifierValue())
                    .append(")");
        }
        reason.append(" not found.");
        return reason.toString();
    }

}
