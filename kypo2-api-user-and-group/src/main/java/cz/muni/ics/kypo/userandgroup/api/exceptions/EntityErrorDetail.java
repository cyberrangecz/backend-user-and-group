package cz.muni.ics.kypo.userandgroup.api.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityErrorDetail {
    @ApiModelProperty(value = "Class of the entity.", example = "IDMGroup")
    private String entity;
    @ApiModelProperty(value = "Identifier of the entity.", example = "id")
    private String identifier;
    @ApiModelProperty(value = "Value of the identifier.", example = "1")
    private Object identifierValue;
    @ApiModelProperty(value = "Detailed message of the exception", example = "Group with same name already exists.")
    private String reason;

    public EntityErrorDetail() {
    }

    public EntityErrorDetail(String reason) {
        this.reason = reason;
    }

    public EntityErrorDetail(Class<?> entityClass, String reason) {
        this(reason);
        this.entity = entityClass.getSimpleName();
    }

    public EntityErrorDetail(Class<?> entityClass, String identifier, Class<?> identifierClass, Object identifierValue, String reason) {
        this(entityClass, reason);
        this.identifier = identifier;
        this.identifierValue = identifierClass.cast(identifierValue);
    }

    public EntityErrorDetail(Class<?> entityClass, String identifier, Class<?> identifierClass, Object identifierValue) {
        this.entity = entityClass.getSimpleName();
        this.identifier = identifier;
        this.identifierValue = identifierClass.cast(identifierValue);
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Object getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(Object identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityErrorDetail entity = (EntityErrorDetail) o;
        return Objects.equals(getEntity(), entity.getEntity()) &&
                Objects.equals(getIdentifier(), entity.getIdentifier()) &&
                Objects.equals(getIdentifierValue(), entity.getIdentifierValue()) &&
                Objects.equals(getReason(), entity.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntity(), getIdentifier(), getIdentifierValue(), getReason());
    }

    @Override
    public String toString() {
        return "EntityErrorDetail{" +
                "entity='" + entity + '\'' +
                ", identifier='" + identifier + '\'' +
                ", identifierValue=" + identifierValue +
                ", reason='" + reason + '\'' +
                '}';
    }
}
