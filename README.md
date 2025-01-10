# CyberRangeCZ Platform User and Group
This project represents back-end for managing users, groups and roles in CyberRangeCZ Platform.

# Repository Structure

The repository is structured with packages as follows:

### .api
Objects that are used for communication between the front-end and the back-end.

- model definitions for transferred and received DTOs.
- mappers

### .definition
Objects used for defining internal working of the application.

- annotations for swagger documentation
- configuration of frameworks and libraries
- exceptions

### .persistence
Objects used for communication with the database.

- entities and their enums
- repositories

### .rest
Objects used for handling HTTP requests.

- controllers
- facades

### .security
Objects used for handling authentication and authorization.

- configuration of identity provider
- configuration of server security
- implementation of security logic
- model of security objects

### .service
Objects used for business logic.

### .startup
Startup hooks.


# Build and Start the Project Using Docker

#### Prerequisities
Install the following technology:

Technology        | URL to Download
----------------- | ------------
Docker            | https://docs.docker.com/install/

#### 1. Preparation of Configuration Files
To build and run the project in docker it is necessary to prepare several configurations:
* Set the [OpenID Connect configuration](https://docs.platform.cyberrange.cz/installation-guide/setting-up-oidc-provider/).
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
* PROJECT_ARTIFACT_ID=user-and-group - the name of the project artifact.
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