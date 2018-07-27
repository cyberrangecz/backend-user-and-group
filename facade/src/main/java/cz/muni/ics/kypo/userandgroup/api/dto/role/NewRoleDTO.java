package cz.muni.ics.kypo.userandgroup.api.dto.role;

import java.util.Objects;

public class NewRoleDTO {

    private String roleType;

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewRoleDTO that = (NewRoleDTO) o;
        return Objects.equals(getRoleType(), that.getRoleType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getRoleType());
    }

    @Override
    public String toString() {
        return "NewRoleDTO{" +
                "roleType='" + roleType + '\'' +
                '}';
    }
}
