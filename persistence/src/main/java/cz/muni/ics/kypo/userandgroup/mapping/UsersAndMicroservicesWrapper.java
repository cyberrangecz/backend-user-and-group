package cz.muni.ics.kypo.userandgroup.mapping;

import cz.muni.ics.kypo.userandgroup.dbmodel.Microservice;

import java.util.ArrayList;
import java.util.List;

public class UsersAndMicroservicesWrapper {

    private List<UserWrapper> users = new ArrayList<>();

    private List<Microservice> microservices = new ArrayList<>();

    public List<UserWrapper> getUsers() {
        return users;
    }

    public void setUsers(List<UserWrapper> users) {
        this.users = users;
    }

    public List<Microservice> getMicroservices() {
        return microservices;
    }

    public void setMicroservices(List<Microservice> microservices) {
        this.microservices = microservices;
    }
}
