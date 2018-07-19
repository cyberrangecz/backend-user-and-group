package cz.muni.ics.kypo.userandgroup.api.dto.role;

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

        NewRoleDTO roleDTO = (NewRoleDTO) o;

        return roleType.equals(roleDTO.roleType);
    }

    @Override
    public int hashCode() {
        return 31 * roleType.hashCode();
    }
}
