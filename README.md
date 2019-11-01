# KYPO2 User and group
This project represents back-end for managing users, groups and roles of KYPO project.

## A: How to set up the project locally 

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


### 2. Creating YAML file with initial users with roles

If you want to insert initial users to the system you have to specify them in external YAML file and then insert its path to 
properties file which is describe in  next step. For each user you have to specify login
(field `sub` from OpenID Connect service) issuer (URI of the OpenId Connect service) and last but not least their main roles. Roles are
three main types - ADMINISTRATOR, USER and GUEST. Every user with role ADMINISTRATOR will also have two other roles 
USER and GUEST. All users with role USER will have also role GUEST. Role GUEST is given to all user in KYPO.

Example of the YAML file can be seen below:
```yaml
- user:
     login: userLogin1
  roles:
     - ROLE_USER_AND_GROUP_ADMINISTRATOR
  iss: https://oidc.muni.cz/oidc/
- user:
     login: userLogin2
  roles:
     - ROLE_USER_AND_GROUP_USER
  iss: https://oidc.muni.cz/oidc/
- user:
     login: userLogin3
  roles:
     - ROLE_USER_AND_GROUP_ADMINISTRATOR
  iss: https://oidc.muni.cz/oidc/
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
After the previous steps you have to create properties file according to format shown in [kypo2 user-and-group property file](kypo2-user-and-group-dev.properties) and save it. 

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

## B: Use Docker to start the project

### 1. Start in DEV mode
The project will run only with HTTP and H2 database. If you would like to run it with HTTPS see steps in [Start in PROD mode](#2-start-in-prod-mode)

#### 1.1 Get OpenId credentials
You need to run [csirtmu-oidc-overlay](https://gitlab.ics.muni.cz/CSIRT-MU/oidc-auth/csirtmu-oidc-overlay) and then use the [default credentials](https://gitlab.ics.muni.cz/CSIRT-MU/oidc-auth/csirtmu-oidc-overlay/blob/1-modify-project-to-run-it-with-initial-bootastrap-sql-file-and-chanage-some-configurations/csirtmu-dummy-issuer/etc/client-openid-credentials.properties)
or change them using a different bootstrap.sql. Another way is to create your own client 
and an protected resource ([OpenId credentials](#1-getting-masaryk-university-openid-connect-credentials)), but URIs may 
differ base on configuration of ***csirtmu-oidc-overlay***, so pay attention what values you set or what URI you refer.    
   
#### 1.2 Creating YAML file with initial users 

Do same steps as [here](#2-creating-yaml-file-with-initial-users-with-roles).

#### 1.3 Properties file
After the previous steps you have to create properties file according to format shown in [kypo2 user-and-group-dev property file](kypo2-user-and-group-dev.properties) and save it.

#### 1.4 Build docker image
Run command: 
```
sudo docker build -t {image name} .
```
e.g.:
```
sudo docker build -t user-and-group .

``` 

#### 1.5 Run docker container
Before you run docker container, make sure that your ***csirtmu-oidc-overlay*** is running.
```
sudo docker run --name {container name} --link {oidc issuer container name}:localhost -it -p {port in host}:{port in container} {user and group docker image}
```
e.g. with this command:
```
sudo docker run --name uag --link oidc_issuer1:localhost -it -p 8084:8084 user-and-group
```
You will run user-and-group project with default [property file](etc/user-and-group.properties) and [initial users](etc/initial-users.yml).
If you want to run it with changed property file use option ***-v*** for docker run command:
```
-v {path to property file}:/app/etc/user-and-group.properties
```
Similarly for initial users file:
```
-v {path to initial users file}:/app/etc/initial-users.yml
```
### 2. Start in PROD mode 

The project will run with HTTPS and PostgreSQL (H2, MySql...) database base on configuration you provide.

#### 2.1 Get OpenId credentials for PROD
Either do same steps as [here](#11-get-openid-credentials), but with [csirtmu-oidc-overlay](https://gitlab.ics.muni.cz/CSIRT-MU/oidc-auth/csirtmu-oidc-overlay)
 configured for HTTPS or you can get [Masaryk University OpenID Connect credentials](#1-getting-masaryk-university-openid-connect-credentials).    
   
#### 2.2 Creating YAML file with initial users 

Do same steps as [here](#2-creating-yaml-file-with-initial-users-with-roles).

#### 2.3 Run database
For example you can run PostgreSQL using docker: 
```
sudo docker run --name {container name} -it -v {path UaG project}/kypo2-persistence-user-and-group/src/main/resources/db/migration/:/docker-entrypoint-initdb.d/ -e POSTGRES_PASSWORD={password} -e POSTGRES_USERNAME={user name} -e POSTGRES_DB={DB name} {postgre image name}
```
e.g.:
```
sudo docker run --name postgres -it home/kypo2-user-and-group/kypo2-persistence-user-and-group/src/main/resources/db/migration/:/docker-entrypoint-initdb.d/ -e POSTGRES_PASSWORD=postgres -e POSTGRES_USERNAME=postgres -e POSTGRES_DB=user-and-group postgres:alpine
```

#### 2.4 Install project
First install it with command bellow:
```
mvn clean install
```
#### 2.5 Migrate DB 
NOTE: Skip this step if you used option ***-v*** (copy initial .sql files) during step [step 3](#23-run-database).

Prerequisites running PostgreSQL ([step 3](#23-run-database)) and created the database named 'user-and-group' with schema 'public'.
To migrate database data it is necessary to run these two scripts in [kypo2-persistence-user-and-group folder](https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-user-and-group/tree/master/kypo2-persistence-user-and-group):

```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://{url to DB}/user-and-group -Djdbc.username={username in DB} -Djdbc.password={password to DB}
```
e.g.:
```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://localhost:5432/user-and-group -Djdbc.username=postgres -Djdbc.password=postgres
```

#### 2.6 Generate CA 
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

Also you can use certificate you already have but make sure that public certificate of ***csirtmu-oidc-overlay*** is in ***trust store***
of application (more specifically in `$JAVA_HOME/lib/security/cacerts`).
For more information about certificates see [wiki](https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/wikis/Create-self-signed-certificate-and-import-it-to-CA).

#### 2.7 Properties file
After the previous steps you have to create properties file according to format shown in [kypo2 user-and-group-prod property file](kypo2-user-and-group-prod.properties) and save it.
For `spring.datasource.url=jdbc:postgresql://{host}:5432/user-and-group` choose name of the database container or exact IP address of the database.  
#### 2.8 Build docker image
Run command: 
```
sudo docker build -t {image name} .
```
e.g.:
```
sudo docker build -t user-and-group .

``` 
NOTE: Before you run command, make sure you have turned on MUNI VPN.

#### 2.9 Run docker container
Before you run docker container, make sure that your OIDC provider is available.
```
sudo docker run --name {container name} -v {path to property file}:/app/etc/user-and-group.properties -v {path to initial users file}:/app/etc/initial-users.yml -v {path to the keystore}/kypo2-keystore.p12:/usr/local/openjdk-11/lib/security/kypo2-keystore.p12 --link {issuer container name or reverse proxy name}:localhost -it -p {port in host}:{port in container} {user and group docker image name}

```
e.g. with this command:
```
sudo docker run --name uag -v /home/user-and-group/user-and-group-project.properties:/app/etc/user-and-group.properties -v /home/user-and-group/users-and-services.yml:/app/etc/initial-users.yml -v /usr/lib/jvm/java-11-openjdk-amd64/lib/security/kypo2-keystore.p12:/usr/local/openjdk-11/lib/security/kypo2-keystore.p12 --link oidc_reverse_proxy:localhost -it -p 8084:8084 user-and-group
```
NOTE: if you are using docker RDBMS for example PostgreSQL add to the command option:
```
--link {name of RDBMS container}:{alias used as host of RDBMS in config file}
```
e.g. you have in config file `spring.datasource.url=jdbc:postgresql://postgres:5432/user-and-group` and you are running RDBMS container with name `postgres` then use:
```
--link postgres:postgres
```
  

