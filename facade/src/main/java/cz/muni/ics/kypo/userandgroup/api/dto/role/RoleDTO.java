package cz.muni.ics.kypo.userandgroup.api.dto.role;

public class RoleDTO {
    private Long id;

    private String roleType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

        RoleDTO roleDTO = (RoleDTO) o;

        if (!id.equals(roleDTO.id)) return false;
        return roleType.equals(roleDTO.roleType);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + roleType.hashCode();
        return result;
    }
}
