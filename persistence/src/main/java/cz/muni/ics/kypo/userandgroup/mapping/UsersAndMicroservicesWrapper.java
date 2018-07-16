package cz.muni.ics.kypo.userandgroup.mapping;

import cz.muni.ics.kypo.userandgroup.mapping.userswrappers.UserWrapper;

import java.util.ArrayList;
import java.util.List;

public class UsersAndMicroservicesWrapper {

    private List<UserWrapper> users = new ArrayList<>();

    public List<UserWrapper> getUsers() {
        return users;
    }

    public void setUsers(List<UserWrapper> users) {
        this.users = users;
    }
}
