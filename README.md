# KYPO User and Group
This project represents back-end for managing users, groups and roles in KYPO platform.

## Content

1. Project modules
2. Build and Start the Project Using Docker

### 1. Project Modules
This project is divided into several modules:
* `kypo-rest-user-and-group`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `kypo-service-user-and-group`
    * Provides business logic of the application:
      * Calls persistence layer for database queries and combining the results as necessary.
      * Calls another microservices.
      * Manages transactions and Aspect Oriented Programming (AOP) mechanisms.
* `kypo-security-user-and-group`
    * Provides security settings:
      * Behaves as Resource Server in OpenID Connect schema.
      * Set Cross-origin Resource Sharing (CORS) to be abble to communicate with clients (Angular).
      * Communicates with Authorization Server.
* `kypo-persistence-user-and-group`
  * Provides data layer of the application (database queries).
  * Uses Spring Data JPA (Spring wrapper layer over JPA implemented with Hibernate framework).
  * Uses QueryDSL for filtering the data.
* `kypo-api-user-and-group`
  * Contains API (DTO classes)
    * These are annotated with proprietary converters for DateTime processing.
    * Localized Bean validations are set (messages are localized).
    * Annotations for Swagger documentation are included.
  * Map Entities to DTO classes and vice versa with MapStruct framework.

And the main project (parent maven project with packaging pom):
* `kypo-user-and-group`
  * Contains configurations for all modules as dependency versions, dependency for spring boot parent project etc.

  


### 2. Build and Start the Project Using Docker

#### Prerequisities
Install the following technology:

Technology        | URL to Download
----------------- | ------------
Docker            | https://docs.docker.com/install/

#### 1. Preparation of Configuration Files
To build and run the project in docker it is necessary to prepare several configurations:
* Set the [OpenID Connect configuration](https://docs.crp.kypo.muni.cz/installation-guide/setting-up-oidc-provider/).
* Modify the [initial-users.yaml](/etc/initial-users.yml) to set initial users and their roles. That file is used to add predefined roles for administrators and key people of the project.
* Fill OIDC credentials gained from the previous step and set additional settings in the [user-and-group.properties](/etc/user-and-group.properties) file and save it.

#### 2. Build Docker Image
The root folder of the project contains a Dockerfile with commands to assemble a docker image.  To build an image run the following command:
```shell
$ sudo docker build \
  --build-arg PROPRIETARY_REPO_URL=https://gitlab.ics.muni.cz/api/v4/projects/2358/packages/maven \
  -t user-and-group-image \
  .
```

Dockefile contains several default arguments:
* USERNAME=postgres - the name of the user to connect to the database. 
* PASSWORD=postgres - user password.
* POSRGRES_DB=user-and-group - the name of the created database.
* PROJECT_ARTIFACT_ID=kypo-rest-user-and-group - the name of the project artifact.
* PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO.

Those arguments can be overwritten during the build of the image, by adding the following option for each argument: 
```shell
--build-arg {name of argument}={value of argument} 
``` 

#### 3. Start the Project
Before you run a docker container, make sure that your ***OIDC Provider*** is running. Instead of usage of the PostgreSQL database, you can use the in-memory database H2. It just depends on the provided configuration. To run a docker container, run the following command: 
```shell
$  sudo docker run \
   --name user-and-group-container -it \
   -p 8084:8084 \
   user-and-group-image
```

Add the following option to use the custom property file:
```shell
-v {path to your config file}:/app/etc/user-and-group.properties
```

To create a backup for your database add the following docker option:
```shell
-v db_data_uag:/var/lib/postgresql/11/main/
```