package cz.muni.ics.kypo.userandgroup.rest.DTO.role;

import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;

public class RoleDTO {
    private Long id;

    private RoleType roleType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleDTO roleDTO = (RoleDTO) o;

        if (!id.equals(roleDTO.id)) return false;
        return roleType == roleDTO.roleType;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + roleType.hashCode();
        return result;
    }
}
