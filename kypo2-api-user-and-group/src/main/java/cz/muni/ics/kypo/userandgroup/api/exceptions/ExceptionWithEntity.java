package cz.muni.ics.kypo.userandgroup.api.exceptions;

public abstract class ExceptionWithEntity extends RuntimeException {
    private EntityErrorDetail entityErrorDetail;

    protected ExceptionWithEntity() {
        super();
    }

    protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail) {
        this.entityErrorDetail = entityErrorDetail;
        if (entityErrorDetail.getReason() == null) {
            this.entityErrorDetail.setReason(createDefaultReason(this.entityErrorDetail));
        }
    }

    protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(cause);
        this.entityErrorDetail = entityErrorDetail;
        if (entityErrorDetail.getReason() == null) {
            this.entityErrorDetail.setReason(createDefaultReason(this.entityErrorDetail));
        }
    }

    protected ExceptionWithEntity(Throwable cause) {
        super(cause);
    }

    public EntityErrorDetail getEntityErrorDetail() {
        return entityErrorDetail;
    }

    /**
     * Method to get default reason of error based on other attributes when no reason is provided.
     * @param entityErrorDetail
     * @return default detailed reason of error.
     */
    protected abstract String createDefaultReason(EntityErrorDetail entityErrorDetail);
}
