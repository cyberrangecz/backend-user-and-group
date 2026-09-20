package cz.cyberrange.platform.userandgroup.definition.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Detail identifying the entity and the identifier value involved in an error, together with the
 * reason to report for it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityErrorDetail {
  @Schema(description = "Type of the entity the failure concerns.", example = "IDMGroup")
  private String entity;

  @Schema(description = "Name of the property the entity was looked up by.", example = "id")
  private String identifier;

  @Schema(description = "Value that property was looked up with.", example = "1")
  private Object identifierValue;

  @Schema(example = "Group with same name already exists.")
  private String reason;

  public EntityErrorDetail() {}

  /**
   * Creates a detail carrying only the given reason. The entity, identifier and identifier value
   * stay unset.
   *
   * @param reason reason to report
   */
  public EntityErrorDetail(@NotBlank String reason) {
    this.reason = reason;
  }

  /**
   * Creates a detail carrying the entity's class name and the given reason. The identifier and
   * identifier value stay unset.
   *
   * @param entityClass class of the entity involved in the error
   * @param reason reason to report
   */
  public EntityErrorDetail(@NotNull Class<?> entityClass, @NotBlank String reason) {
    this(reason);
    this.entity = entityClass.getSimpleName();
  }

  /**
   * Creates a detail carrying the entity's class name, its identifier name and value, and the given
   * reason.
   *
   * @param entityClass class of the entity involved in the error
   * @param identifier name of the identifier used to look up the entity
   * @param identifierClass type the identifier value is cast to
   * @param identifierValue value of the identifier
   * @param reason reason to report
   * @throws ClassCastException when identifierValue is not an instance of identifierClass
   */
  public EntityErrorDetail(
      @NotNull Class<?> entityClass,
      @NotNull String identifier,
      @NotNull Class<?> identifierClass,
      @NotNull Object identifierValue,
      @NotBlank String reason) {
    this(entityClass, reason);
    this.identifier = identifier;
    this.identifierValue = identifierClass.cast(identifierValue);
  }

  /**
   * Creates a detail carrying the entity's class name and its identifier name and value. The reason
   * stays unset.
   *
   * @param entityClass class of the entity involved in the error
   * @param identifier name of the identifier used to look up the entity
   * @param identifierClass type the identifier value is cast to
   * @param identifierValue value of the identifier
   * @throws ClassCastException when identifierValue is not an instance of identifierClass
   */
  public EntityErrorDetail(
      @NotNull Class<?> entityClass,
      @NotNull String identifier,
      @NotNull Class<?> identifierClass,
      @NotNull Object identifierValue) {
    this.entity = entityClass.getSimpleName();
    this.identifier = identifier;
    this.identifierValue = identifierClass.cast(identifierValue);
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(@NotBlank String entity) {
    this.entity = entity;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(@NotBlank String identifier) {
    this.identifier = identifier;
  }

  public Object getIdentifierValue() {
    return identifierValue;
  }

  public void setIdentifierValue(@NotNull Object identifierValue) {
    this.identifierValue = identifierValue;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(@NotBlank String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityErrorDetail entity = (EntityErrorDetail) o;
    return Objects.equals(getEntity(), entity.getEntity())
        && Objects.equals(getIdentifier(), entity.getIdentifier())
        && Objects.equals(getIdentifierValue(), entity.getIdentifierValue())
        && Objects.equals(getReason(), entity.getReason());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEntity(), getIdentifier(), getIdentifierValue(), getReason());
  }

  @Override
  public String toString() {
    return "EntityErrorDetail{"
        + "entity='"
        + entity
        + '\''
        + ", identifier='"
        + identifier
        + '\''
        + ", identifierValue="
        + identifierValue
        + ", reason='"
        + reason
        + '\''
        + '}';
  }
}
