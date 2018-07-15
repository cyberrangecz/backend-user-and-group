package cz.muni.ics.kypo.userandgroup.mapping;

import cz.muni.ics.kypo.userandgroup.mapping.roleswrappers.ServiceAndRoles;
import cz.muni.ics.kypo.userandgroup.mapping.userswrappers.UserWrapper;

import java.util.ArrayList;
import java.util.List;

public class RolesAndUsersWrapper {

    private List<ServiceAndRoles> roles = new ArrayList<>();

    private List<UserWrapper> users = new ArrayList<>();

    public List<ServiceAndRoles> getRoles() {
        return roles;
    }

    public void setRoles(List<ServiceAndRoles> roles) {
        this.roles = roles;
    }

    public List<UserWrapper> getUsers() {
        return users;
    }

    public void setUsers(List<UserWrapper> users) {
        this.users = users;
    }
}
