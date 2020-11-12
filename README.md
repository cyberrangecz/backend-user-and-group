# KYPO User and Group
This project represents back-end for managing users, groups and roles in KYPO platform.

## Content

1.  Requirements
2.  Project modules

### 1. Requirements
The requirements for manual installation of the project, building and running the project in docker container and so on are available on the following [WIKI](https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-user-and-group/wikis/home) page.

### 2. Project Modules
This project is containes several modules. 
It is divided into several modules as:
* `kypo2-rest-user-and-group`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `kypo2-service-user-and-group`
    * Provides business logic of the application:
      * Calls persistence layer for database queries and combining the results as necessary.
      * Calls another microservices.
      * Manages transactions and Aspect Oriented Programming (AOP) mechanisms.
* `kypo2-security-user-and-group`
    * Provides security settings:
      * Behaves as Resource Server in OpenID Connect schema.
      * Set Cross-origin Resource Sharing (CORS) to be abble to communicate with clients (Angular).
      * Communicates with Authorization Server.
* `kypo2-persistence-user-and-group`
  * Provides data layer of the application (database queries).
  * Uses Spring Data JPA (Spring wrapper layer over JPA implemented with Hibernate framework).
  * Uses QueryDSL for filtering the data.
* `kypo2-api-user-and-group`
  * Contains API (DTO classes)
    * These are annotated with proprietary converters for DateTime processing.
    * Localized Bean validations are set (messages are localized).
    * Annotations for Swagger documentation are included.
  * Map Entities to DTO classes and vice versa with MapStruct framework.

And the main project (parent maven project with packaging pom):
* `kypo2-user-and-group`
  * Contains configurations for all modules as dependency versions, dependency for spring boot parent project etc.
