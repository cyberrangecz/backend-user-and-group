package cz.muni.ics.kypo.userandgroup.api.exceptions;

public abstract class ExceptionWithEntity extends RuntimeException {
    private EntityErrorDetail entityErrorDetail;

    protected ExceptionWithEntity() {
        super();
    }

    protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail) {
        this.entityErrorDetail = entityErrorDetail;
    }

    protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(cause);
        this.entityErrorDetail = entityErrorDetail;
    }

    protected ExceptionWithEntity(Throwable cause) {
        super(cause);
    }

    public EntityErrorDetail getEntityErrorDetail() {
        return entityErrorDetail;
    }
}
