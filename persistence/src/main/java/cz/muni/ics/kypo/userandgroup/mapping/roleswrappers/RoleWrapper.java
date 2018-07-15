package cz.muni.ics.kypo.userandgroup.mapping.roleswrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RoleWrapper {

    private String roleType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<RoleWrapper> children = new HashSet<>();

    @JsonCreator
    public RoleWrapper(@JsonProperty(value = "roleType", required = true) String roleType,
                       @JsonProperty(value = "childre", required = false) Set<RoleWrapper> children) {
        this.roleType = roleType;
        if (children == null) {
            this.children = new HashSet<>();
        } else {
            this.children = children;
        }
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public Set<RoleWrapper> getChildren() {
        return children;
    }

    public void setChildren(Set<RoleWrapper> children) {
        if (children == null) {
            this.children = new HashSet<>();
        } else {
            this.children = children;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleWrapper that = (RoleWrapper) o;
        return Objects.equals(getRoleType(), that.getRoleType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getRoleType());
    }
}
