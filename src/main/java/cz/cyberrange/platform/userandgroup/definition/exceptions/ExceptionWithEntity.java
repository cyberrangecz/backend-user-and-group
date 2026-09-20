package cz.cyberrange.platform.userandgroup.definition.exceptions;

/**
 * Base for exceptions that report a failure tied to one entity, carrying the entity's identity and
 * the reason to report alongside it.
 */
public abstract class ExceptionWithEntity extends RuntimeException {
  // Stays null when the exception was created without an entity detail.
  private EntityErrorDetail entityErrorDetail;

  protected ExceptionWithEntity() {
    super();
  }

  /**
   * Wraps the given entity detail, filling its reason with the subclass's default reason when none
   * was supplied.
   *
   * @param entityErrorDetail detail describing the entity involved in the failure
   */
  protected ExceptionWithEntity(EntityErrorDetail entityErrorDetail) {
    this.entityErrorDetail = entityErrorDetail;
    if (entityErrorDetail.getReason() == null) {
      this.entityErrorDetail.setReason(createDefaultReason(this.entityErrorDetail));
    }
  }

  /**
   * Wraps the given entity detail and cause, filling the detail's reason with the subclass's
   * default reason when none was supplied.
   *
   * @param entityErrorDetail detail describing the entity involved in the failure
   * @param cause the cause of the failure
   */
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
   * Builds the reason text reported when the entity detail carries none of its own.
   *
   * @param entityErrorDetail detail describing the entity involved in the failure
   * @return the reason text to report
   */
  protected abstract String createDefaultReason(EntityErrorDetail entityErrorDetail);
}
