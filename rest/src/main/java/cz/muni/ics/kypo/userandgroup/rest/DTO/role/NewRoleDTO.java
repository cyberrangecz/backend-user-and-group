package cz.muni.ics.kypo.userandgroup.rest.DTO.role;

import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;

public class NewRoleDTO {

    private RoleType roleType;

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

        NewRoleDTO roleDTO = (NewRoleDTO) o;

        return roleType == roleDTO.roleType;
    }

    @Override
    public int hashCode() {
        return 31 * roleType.hashCode();
    }
}
