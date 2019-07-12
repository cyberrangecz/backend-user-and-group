# KYPO2 User and group
This project represents back-end for managing users, groups and roles of KYPO project.

## How to set up the project

### 1. Getting Masaryk University OpenID Connect credentials 

1. Go to `https://oidc.muni.cz/oidc/` and log in.
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

If you want to insert initial users to the system you have to specify them in external YAML file and then insert its path to 
properties file which is describe in  next step. For each user you have to specify their screen name, full name, email
(these information has to be same as in OpenID Connect service) and last but not least their main roles. Roles are
three main types - ADMINISTRATOR, USER and GUEST. Every user with role ADMINISTRATOR will also have two other roles 
USER and GUEST. All users with role USER will have also role GUEST. Role GUEST is given to all user in KYPO.
 
Below that you can specify all microservices and their endpoint. The service user-and-group will be managing 
roles of the microservices.

Example of the YAML file can be seen below:
```yaml
- user:
     login: userLogin1
  roles:
     - ROLE_USER_AND_GROUP_ADMINISTRATOR
- user:
     login: userLogin2
  roles:
     - ROLE_USER_AND_GROUP_USER
- user:
     login: userLogin3
  roles:
     - ROLE_USER_AND_GROUP_ADMINISTRATOR
```
## 3. Generate CA for project 
Use 'keytool' to generate KeyStore for client:

```
keytool -genkeypair -alias {alias of KeyStore} -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore {filename of KeyStore}.p12 -validity 3650
```
It will create file with private key, public key and certificate for client. During generating the KeyStore, use for 'first name and last name' domain name of server where your application is running, e.g., localhost.
  
Then export certificate from KeyStore to create *.crt file:
```
keytool -export -keystore {filename of created KeyStore}.p12 -alias {alias of certificate} -file {filename of certificate}.crt
```

After that import exported certificate into TrustStore:
```
keytool -importcert -trustcacerts -file {path to exported certificate} -alias {alias of exported certificate} -keystore {path to your TrustStore}
```

To remove certificate from TrustStore:
```
keytool -delete -alias {alias of certificate} -keystore {path to TrustStore}
```

To show all certificates in TrustStore: 
```
keytool -list -v -keystore {path to TrustStore}
```

For more information about 'How to enable communication over https between 2 spring boot applications using self signed certificate' visit http://www.littlebigextra.com/how-to-enable-communication-over-https-between-2-spring-boot-applications-using-self-signed-certificate

### 4. Properties file
After the previous steps you have to create properties file according to format shown in [kypo2 user-and-group property file](kypo2-user-and-group.properties) and save it. 

## Start the project

### Installing the project
First install it with command bellow:
```
mvn clean install
```

### Database migration
Prerequisites running PostgreSQL and created the database named 'user-and-group' with schema 'public'.
To migrate database data it is necessary to run these two scripts:

```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://{url to DB}/user-and-group -Djdbc.username={username in DB} -Djdbc.password={password to DB}
```
e.g.:
```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://localhost:5432/user-and-group -Djdbc.username=postgres -Djdbc.password=postgres
```

NOTE: This script must be run in [persistence] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-user-and-group/tree/master/persistence) module.

### Start the project

To start the project you have to go to module `rest` and start it:
```
cd rest/
mvn spring-boot:run -Dpath.to.config.file={path to properties file from step 3}
```

Then system will start the project. You can access the REST API with Swagger UI on address: 
`~/{context path for service}/swagger-ui.html`. You can log in there and interact with the service.

