# KYPO2 User and group
This project represents back-end for managing users, groups and roles of KYPO project.

## How to set up the project

### 1. Getting Masaryk University OpenID Connect credentials 

1. Go to `https://oidc.ics.muni.cz/oidc/` and log in.
2. Click on "**Self-service Client Registration**" -> "**New Client**".
3. Set Client name.
4. Add at least one custom Redirect URI and `http://localhost:8080/{context path from external properties file}/webjars/springfox-swagger-ui/oauth2-redirect.html` (IMPORTANT for Swagger UI).
5. In tab "**Access**":
    1. choose which information about user you will be getting, so called `scopes`.
    2. select just *implicit* in **Grand Types**
    3. select *token* and *code id_toke* in **Responses Types**
6. Hit **Save** button.
7. Then got to tab "**JSON**", copy the JSON file and save it to file. **IMPORTANT STEP**
8. Now create new Resource in "**Self-service Protected Resource Registration**".
9. Again insert client Name and save JSON to external file in "**JSON**" tab.
10. In tab "**Access**" again choose which information about user you will be getting, so called `scopes`.
11. Hit **Save** button.


### 2. Creating YAML file with roles and initial users

If you want to insert new roles to the system you have to specify them in external YAML file and then insert its path to 
properties file which is describe in  next step. For each role you have to specify name of service for which it is and 
name of the role. To the same YAML file you can also write down users who will be insert to the system during start up 
of project. You have to also specify roles of the users in the file. Example of the YAML file can be seen below:
```yaml
roles:
    services:
        - name: service1
          roles:
            - ROLE1
            - ROLE2
        - name: service2
          roles: 
            - ROLE3

users:
    ROLE1:
        - screenName: userOne
          fullName: User One
          mail: user.one@mail.com
        - screenName: userTwo
          fullName: User Two
          mail: user.two@mail.com
    ROLE2:
        - screenName: userOne
          fullName: User One
          mail: user.one@mail.com
```

### 3. Properties file

After step 2 you have to create properties file according to format below and save it.
```properties
server.port={port for service}
server.servlet.context-path=/{context path for service}

# Logging
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
logging.level.org.mitre.openid.connect.binder.service=DEBUG

# OpenID Connect
kypo.idp.4oauth.introspectionURI=https://oidc.ics.muni.cz/oidc/introspect
kypo.idp.4oauth.authorizationURI=https://oidc.ics.muni.cz/oidc/authorize
kypo.idp.4oauth.resource.clientId={your client ID from Self-service protected resource}
kypo.idp.4oauth.resource.clientSecret={your client secret from Self-service protected resource}
kypo.idp.4oauth.client.clientId={your client ID from Self-service client}
kypo.idp.4oauth.scopes=openid, email 
# you can add more scopes according to settings from step 1.

# DATASOURCE
spring.datasource.url=jdbc:postgresql://{url to DB}
spring.datasource.username={user in DB}
spring.datasource.password={password for user to DB}
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults = false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL9Dialect


# FLYWAY
spring.flyway.url=jdbc:postgresql://{url to DB}
spring.flyway.user={user in DB}
spring.flyway.password={password for user to DB}
spring.flyway.table=schema_version

path.to.file.with.initial.roles.and.users={path to YAML file from step 2}
```

## Start the project

First install it with command bellow:
```
mvn install -Dpath-to-config-file={path to properties file from step 3}
```

To start the project you have to go to module `rest` and start it:
```
cd rest/
mvn spring-boot:run -Dpath-to-config-file={path to properties file from step 3}
```

Then system will start the project. You can access the REST API with Swagger UI on address: 
`~/{context path for service}/swagger-ui.html`. You can log in there and interact with the service.

