* bd05786 -- Updated Swagger documentation generated.
* d8a456f -- Update pom.xml version based on GitLab tag. Done by CI.
* c756f3c -- Update VERSION.txt
*   7e0425e -- Merge branch '151-automaticly-generate-open-api-based-on-swagger' into 'master'
|\  
| * 61d7a1a -- Added plugin to generate OpenAPI into doc-files folder
|/  
* 9e8aba6 -- Update .gitlab-ci.yml
* be930da -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* 6375340 -- Update pom.xml version based on GitLab tag. Done by CI.
* 4ae61ff -- Update VERSION.txt
* 2174951 -- Update .gitlab-ci.yml
* 3fd1ec2 -- Update .gitlab-ci.yml
* c19eddd -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* 45fc0da -- Update pom.xml version based on GitLab tag. Done by CI.
* 6e7896a -- Update VERSION.txt
*   d4774ef -- Merge branch '150-check-the-code-and-remove-dev-profile-if-not-necessary' into 'master'
|\  
| * 05c9993 -- Removed DEV and PROD profiles
|/  
* 84a1672 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* 7ff859a -- Update pom.xml version based on GitLab tag. Done by CI.
* b33eefd -- Update VERSION.txt
*   bb03058 -- Merge branch '149-edit-dockerfiles-so-only-essential-files-are-copied' into 'master'
|\  
| * f8ee97f -- Resolve "Edit Dockerfiles so only essential files are copied"
|/  
* 9ddbd91 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* e02a399 -- Update pom.xml version based on GitLab tag. Done by CI.
* 077c85e -- Update VERSION.txt
*   d2e0061 -- Merge branch '148-use-v-prefix-for-tags' into 'master'
|\  
| * f68a386 -- Resolve "Use 'v' prefix for tags"
|/  
*   c4548ae -- Merge branch '147-method-to-create-user-in-customauthoritygranter-creates-multiple-users' into 'master'
|\  
| * fe3c994 -- Added @Repeatable annotation above createOrUpdateOrGetUser, created concurrent test to create user
* | b7f3e52 -- Update kypo2-user-and-group-prod.properties
* | e06f868 -- Update kypo2-user-and-group-dev.properties
* | 4de689e -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* | e8b6b75 -- Update pom.xml version based on GitLab tag. Done by CI.
* | 771a17c -- Update VERSION.txt
* |   c16b0c7 -- 	Merge branch 'master' of gitlab.ics.muni.cz:kypo-crp/backend-java/kypo2-user-and-group
|\ \  
| * | 963cc56 -- Update supervisord.conf
* | | a5c7004 -- add logback-spring.xml as an example logback configuration
|/ /  
* |   feb695d -- Merge branch '146-allow-change-debug-level-in-property-file' into 'master'
|\ \  
| |/  
|/|   
| * 1ebfb3b -- Resolve "Allow change debug level in property file"
|/  
*   834d6bb -- Merge branch '145-update-flyway-migration-files-based-on-the-current-jpa-entity-definitions' into 'master'
|\  
| * aecdb9e -- Resolve "Update flyway migration files based on the current JPA entity definitions"
|/  
*   b92c014 -- Merge branch '144-disable-log-rotation-in-supervisord' into 'master'
|\  
| * 629ca16 -- Log rotation updated
* | b060bd2 -- Update supervisord.conf
|/  
* 099bab3 -- Update supervisord.conf
* eee92ab -- Update supervisord.conf
* 526af47 -- Update supervisord.conf
* f94f856 -- Update Dockerfile
* e6cec9a -- Update initial-users.yml
* 55b0c88 -- Add configuration to print logs from supervisor.d to the console
* 0f02acb -- Update Dockerfile
*   d5bd15c -- Merge branch '142-create-apientityerror-as-subclass-of-apierror' into 'master'
|\  
| * e180623 -- Create subclass ApiEntityError
|/  
*   42f5493 -- Merge branch '133-add-pageable-for-getrolesofuser-and-getrolesofgroup-endpoints' into 'master'
|\  
| * 96cf5b1 -- Resolve "Add pageable for getRolesOfUser and getRolesOfGroup endpoints"
|/  
* 0880fdd -- Update .gitlab-ci.yml
* 3bcc554 -- Update initial-users.yml
*   f649f4d -- Merge branch '141-rename-login-to-sub-in-db-and-all-the-necessary-object-files-etc' into 'master'
|\  
| * f300a56 -- replace login with sub.
|/  
*   d83ff5f -- Merge branch '140-remove-resttemplate-configuration' into 'master'
|\  
| * 07bf058 -- Removed RestTemplateConfiguration and rest template interceptor
|/  
* dddd4d2 -- Update initial-users.yml
* 97aa008 -- remove pessimistic lock since it is not necessary.
* 2fbdc52 -- Pessimistic READ for findBySubAndLogin added.
*   9c362c3 -- Merge branch '139-make-consistent-rest-resource-in-path-variable-names' into 'master'
|\  
| * c53e8a1 -- refactor PathVariable names to be consistent throughout the API.
|/  
*   50a5490 -- Merge branch '138-create-index-for-iss-field' into 'master'
|\  
| * 6036b7c -- Resolve "Create index for iss field"
|/  
* b368f5c -- Update Dockerfile
* e48c5c5 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* a179d33 -- Update pom.xml version based on GitLab tag. Done by CI.
* bed5cec -- Update VERSION.txt
* 63a8846 -- Update .gitlab-ci.yml
* 7441f73 -- Update .gitlab-ci.yml
* 8f946fd -- Update .gitlab-ci.yml
* 2a4916e -- Update .gitlab-ci.yml
* 42aacb3 -- Delete changelog.sh
*   5770907 -- Merge branch '136-refactor-queries-to-named-queries' into 'master'
|\  
| * 3e41bb7 -- refactor queries to named queries.
|/  
*   dbb52c4 -- Merge branch '135-remove-debian-package-from-that-project' into 'master'
|\  
| * 21bd8ba -- Delete Debian package.
|/  
* 89af939 -- Update initial-users.yml
* f6b6752 -- Update initial-users.yml
* c8918de -- Update initial-users.yml
* bd8d1c8 -- Update pom.xml version based on GitLab tag. Done by CI.
* ac9f605 -- Update VERSION.txt
* 07e6187 -- Update .gitlab-ci.yml
* 1047ea8 -- Update VERSION.txt
*   d926611 -- Merge branch '134-convert-frequently-called-queries-to-namedqueries' into 'master'
|\  
| * b14f04c -- Resolve "Convert Frequently called queries to NamedQueries"
|/  
* 5258e22 -- Update VERSION.txt
* 1ae9755 -- fix tests.
* ea50008 -- Update pom.xml
* 428f607 -- Update .gitlab-ci.yml
* c4f5807 -- Upgrade spring boot parent pom version to 2.2.5
* 97a4757 -- Update VERSION.txt
* b929be7 -- Update .gitlab-ci.yml
* ef528cb -- Update .gitlab-ci.yml
* 09dd160 -- Update .gitlab-ci.yml
* 2bd5f0c -- Update .gitlab-ci.yml
* 53635d2 -- Update .gitlab-ci.yml
* 3a491c2 -- Update VERSION.txt
* 3aff2fe -- Update VERSION.txt
* 9aba957 -- Update .gitlab-ci.yml
* 787b29d -- Update VERSION.txt
* 0b793ac -- Update pom.xml version based on GitLab tag. Done by CI.
* 5b6014e -- Update VERSION.txt
* b812ba9 -- Update .gitlab-ci.yml
*   7454ce8 -- Merge branch '132-repair-swagger-schema-error' into 'master'
|\  
| * 7a3e9ac -- Repaired swagger schema error
|/  
* 482d6b3 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* 5a90853 -- Update pom.xml version based on GitLab tag. Done by CI.
* 29df480 -- Update VERSION.txt
* 3ecd7f2 -- Commit changes to CHANGELOG.md file
* ecd0bfe -- Update pom.xml version based on GitLab tag. Done by CI.
* 68be0ba -- Update VERSION.txt
* 756c942 -- Update .gitlab-ci.yml
* 3b2e1b9 -- Update .gitlab-ci.yml
* 82df15d -- Update .gitlab-ci.yml
* 5ca62ec -- Update .gitlab-ci.yml
* 8cc3872 -- Do not run tests in the case of creating new tag
* a06be07 -- Update .gitlab-ci.yml
*   23bb7af -- Merge branch 'master' of gitlab.ics.muni.cz:kypo-crp/backend-java/kypo2-user-and-group
|\  
| * 4ad9cab -- Update pom.xml version based on GitLab tag. Done by CI.
* | 9ff4b64 -- Add changelog.md.
|/  
* cc62e07 -- Test Gitlab CI.
* 8c5042a -- Update .gitlab-ci.yml
* bd154c4 -- Update pom.xml version based on GitLab tag. Done by CI.
* 444e0e5 -- update version.
* 849b8d6 -- Update .gitlab-ci.yml
* 6d22f8d -- Update .gitlab-ci.yml
* 8e58a25 -- Update .gitlab-ci.yml
* c0f5987 -- Update pom.xml version based on GitLab tag. Done by CI.
* 61b8b8c -- generation of change log added.
* 5554071 -- Save Git logs in CHANGELOG.md file
* bbca32c -- Update .gitlab-ci.yml
* e7029f6 -- Update pom.xml version based on GitLab tag. Done by CI.
*   4834efe -- Merge branch 'master' of gitlab.ics.muni.cz:kypo-crp/backend-java/kypo2-user-and-group
|\  
| * 0299216 -- Update pom.xml version based on GitLab tag. Done by CI.
* | 532ec01 -- test
|/  
* 813a0ed -- cut single characters instead of s.
* 6f2c6ff -- cut single characters instead of s.
* 7c22258 -- update versions.
* c3892a4 -- Update CI.
* 01687e9 -- Gitlab CI updated.
* 38f02c8 -- git push skip ci for createTag
* 8ae2e23 -- change version.
* 3578d30 -- Update .gitlab-ci.yml
* b5d5662 -- User and Group project Gitlab CI set.
* 87731d3 -- The variable does not work inside only: except/changes part in .gitlab-ci.yml
* 1a4fec2 -- Update .gitlab-ci.yml
* 0ac85e1 -- Update .gitlab-ci.yml
* 5f5307d -- Update .gitlab-ci.yml
* 79daea7 -- Test Gitlab CI
* e18540b -- Add link to proprietary repo URL to optimize the builds in CI through Nexus Repo
*   d00b1c3 -- Merge branch '121-prepare-ci-to-docker-instead-of-debian' into 'master'
|\  
| * fa9fd68 -- Resolve "Prepare CI to Docker instead of Debian"
|/  
* 10ec245 -- Update Dockerfile
* defae6a -- Update pom.xml version based on GitLab tag. Done by CI
* 64908b1 -- Update pom.xml version based on GitLab tag. Done by CI
*   be54ec0 -- Merge branch '131-remove-asserts-in-service-and-facade-layer' into 'master'
|\  
| * 97fb882 -- Resolve "Remove Asserts in Service and Facade Layer"
|/  
*   6fbb162 -- Merge branch '125-refactor-exception-handling' into 'master'
|\  
| * 2e510a0 -- Resolve "Refactor exception handling"
|/  
*   5c901b2 -- Merge branch '124-update-unit-tests' into 'master'
|\  
| * 7793385 -- Resolve "Update unit tests"
|/  
*   3752d24 -- Merge branch '115-add-nexus-repository-to-pom-xml' into 'master'
|\  
| * d3b7d02 -- Resolve "Add nexus repository to pom.xml"
|/  
*   7bbe26a -- Merge branch '120-set-username-and-password-for-database-to-be-overridable-during-start-of-the-container' into 'master'
|\  
| * 8a1e2d7 -- Resolve "Set username and password for database to be overridable during start of the container"
|/  
*   0507a8f -- Merge branch '114-remove-nexus-repositories-with-kypo-dependencies-distributionmanagement' into 'master'
|\  
| * 59f1fd8 -- Distribution management in pom.xm removed.
* |   28e2664 -- Merge branch '117-remove-own-aop-functionality-from-the-project' into 'master'
|\ \  
| |/  
|/|   
| * 0b166de -- Remove own aop definition.
|/  
*   2b63248 -- Merge branch '116-remove-pdf-generation-from-rest-module-including-jcenter-dependency' into 'master'
|\  
| * 6f743b6 -- Remove jcenter dependencies and the generation of REST API doc.
|/  
*   8d69536 -- Merge branch '111-add-logback-xml-to-the-project-for-setting-logs' into 'master'
|\  
| * 3ddf3de -- Remove logs from Spring Boot example .properties.
|/  
*   95f7b13 -- Merge branch '112-add-logback-to-the-project' into 'master'
|\  
| * 69b4cc1 -- Add logback file.
|/  
*   f48a9e3 -- Merge branch '102-write-missing-integration-tests' into 'master'
|\  
| * 1ac170b -- Resolve "Write missing integration tests"
|/  
*   9ed43a2 -- Merge branch '110-change-import-of-class-accessdeniedexception-in-customrestexceptionhandler' into 'master'
|\  
| * 718df95 -- changed import of access denied exception
|/  
*   1e656be -- Merge branch '106-create-data-factory-for-test-entities' into 'master'
|\  
| * 9dfbee7 -- Resolve "Create data factory for test entities"
|/  
*   4b45d91 -- Merge branch '109-refactor-methods-in-facade-classes' into 'master'
|\  
| * 5a1d594 -- Resolve "Refactor methods in facade classes"
|/  
*   830ac96 -- Merge branch '108-update-docker-with-supervisord' into 'master'
|\  
| * e14a915 -- Updated Dockerfile and added supervisord.conf file
|/  
*   d7ca5a1 -- Merge branch '107-change-docker-openjdk-to-slim-version' into 'master'
|\  
| * 617c39b -- openjdk changed to adoptopenjdk for docker
|/  
*   2dea9db -- Merge branch '104-refactor-and-clean-code' into 'master'
|\  
| * 4a59e6f -- Resolve "Refactor and clean code"
|/  
* 0508d79 -- testDataFactroy set as spring component
* 58d0030 -- Specific test groups added to test data factory
* 7f80897 -- Specific test users added to test data factory
* a684425 -- specific role and microservice generation added to TestDataFactory
* a9f07fb -- TestDataFactory class created
* 4878eae -- Update pom.xml version based on GitLab tag. Done by CI
* 03a088b -- Update README.md
* 9191951 -- Update copyright
* 53068a6 -- Add LICENSE
*   9f40f70 -- Merge branch '105-remove-author-tag' into 'master'
|\  
| * 5d70a0d -- Resolve "Remove author tag"
|/  
*   09c62d6 -- Merge branch '103-remove-all-comments' into 'master'
|\  
| * 3ed1549 -- Resolve "Remove all comments"
|/  
* 04d80af -- Update pom.xml version based on GitLab tag. Done by CI
* bff11ff -- change authorization access to GUEST for getting user by id.
* b691fe1 -- Update pom.xml version based on GitLab tag. Done by CI
*   4ecc5b0 -- Merge branch '101-distict-query-result-in-userrepositoryimpl' into 'master'
|\  
| * de3dd8d -- Resolve "Distict query result in UserRepositoryImpl"
|/  
* 207c184 -- Update pom.xml version based on GitLab tag. Done by CI
*   2d4d89b -- Merge branch '100-fix-impossible-resource-filtering-on-several-rest-resources' into 'master'
|\  
| * 9b5c91b -- Resolve "fix impossible resource filtering on several rest resources"
|/  
* fb0b569 -- Update pom.xml version based on GitLab tag. Done by CI
*   36a776d -- Merge branch '98-fix-role-access-for-accessing-users-with-ids' into 'master'
|\  
| * ba640e1 -- Resolve "Fix role access for accessing users with ids"
|/  
*   023c64c -- Merge branch '97-add-apimodel-to-the-dto-classes' into 'master'
|\  
| * 986f614 -- Resolve "Add @ApiModel to the DTO classes"
|/  
*   4a68834 -- Merge branch '96-api-model-property-adjustment-in-dtos' into 'master'
|\  
| * cb7ab19 -- Resolve "Api model property adjustment in DTOs"
|/  
* b52a772 -- Update README.md
* 04deda3 -- Add plugins to generate .pdf and asciidoc documentation.
* 61ad7c9 -- Update pom.xml version based on GitLab tag. Done by CI
* 9049207 -- call dh_install while overriding it
* 94a1b9f -- Update pom.xml version based on GitLab tag. Done by CI
*   9a0d8e3 -- Merge branch '94-fix-missing-pagination-in-get-all-roles-rest-resource' into 'master'
|\  
| * 5ec14c8 -- Pagination for roles fixed.
|/  
*   a8acb93 -- Merge branch '95-fix-filtering-on-groups' into 'master'
|\  
| * 247bdc8 -- Resolve "Fix filtering on groups"
|/  
* 4e46a8d -- Fix tabs and spaces in debian/rules
* a7660a5 -- Update rules, add mv rule
*   5b4a4a8 -- Merge branch '92-create-dev-and-prod-profiles-and-dockerize-project' into 'master'
|\  
| * 81baa91 -- Resolve "Create dev and prod profiles and dockerize project"
|/  
* 1221b96 -- Update pom.xml version based on GitLab tag. Done by CI
*   b42af42 -- Merge branch '93-make-mapped-abstractentity-class' into 'master'
|\  
| * 7c007c0 -- Provide AbstractEntity mappedsuperclass for entities to set ID column.
|/  
* 84de2b6 -- Removed entity manager from rest controller.
* 97f82fa -- Add implements serializable.
*   d60f382 -- Merge branch '89-create-pagination-for-endpoint-to-obtain-users-with-given-ids' into 'master'
|\  
| * 742d211 -- Resolve "Create pagination for endpoint to obtain users with given ids"
|/  
*   a3a63b8 -- Merge branch '90-response-from-users-not-in-given-group-return-big-number-of-elements' into 'master'
|\  
| * c800d15 -- Changed countQuery in usersNotInGivenGroup
|/  
* 200335a -- Set spring.jpa.show-sql to false by default in kypo2-user-and-group.properties.
* 16b933f -- Removed caching configuration
* 354a34a -- Configuration of caching
*   5d6c52f -- Merge branch '85-generate-identicon-on-user-registration' into 'master'
|\  
| * 3032364 -- Resolve "Generate identicon on user registration"
|/  
*   612a07c -- Merge branch '87-move-cors-configuration-to-the-separate-class-corsfilter' into 'master'
|\  
| * 8c85836 -- Cors filter moved to the separate class
|/  
* 73dc9aa -- Update pom.xml version based on GitLab tag. Done by CI
*   ac6db07 -- Merge branch '84-fix-cors-spring-bean-postprocessing' into 'master'
|\  
| * 00682d3 -- spring bean for cors based as primary filter.
|/  
* 0efbc5d -- Update pom.xml version based on GitLab tag. Done by CI
* f96e652 -- evaluate querydsl expressions as and.
* 0092085 -- refactor querydsl to or from and.
*   d9b536b -- Merge branch '83-express-string-queries-in-querydsl-using-contains' into 'master'
|\  
| * 77e9ebb -- Resolve "Express string queries in QueryDSL using contains"
|/  
*   c6f4da7 -- Merge branch '82-case-insensitive-for-querydsl-queries' into 'master'
|\  
| * 10e5aa4 -- equals ignore case for all string attributes in the query.
|/  
* a1ad06e -- Cors allowed origins added to the property file example.
*   6c6ee16 -- Merge branch '81-rename-cors-path-attribute-to-allowed-origins' into 'master'
|\  
| * e8132df -- possibility to configure allowerd origins for cors path mapping.
|/  
* 77f6e4b -- Remove outer JOINS.
* 1fafed3 -- CountQueries specified with Count in SQL.
* f43a257 -- Update pom.xml version based on GitLab tag. Done by CI
*   8aa25e5 -- Merge branch '80-make-a-check-if-fields-from-introspection-response-are-present' into 'master'
|\  
| * 6b323b3 -- Check possibly non field in introspection response
|/  
* 135247e -- Update pom.xml version based on GitLab tag. Done by CI
*   1d0e85a -- Merge branch '79-enable-or-disable-swagger-using-file-property' into 'master'
|\  
| * c93d533 -- Add additional flag for enabling or disabling swagger ui.
|/  
*   7274657 -- Merge branch '78-common-configuration-of-multiple-providers' into 'master'
|\  
| * fdb8d2d -- Changed configuration for multiple providers and modified kypo2-user-and-group.properties
|/  
*   66fbf87 -- Merge branch '77-modify-readme-add-configuration-of-initial-users' into 'master'
|\  
| * 99fe36a -- Modified README configuration of initial users - added issuer
|/  
*   afb7251 -- Merge branch '76-uncomment-code-to-connect-to-second-oidc-provider' into 'master'
|\  
| * a70fa94 -- Code to integrate second OIDC provider uncommented.
|/  
*   1dbec40 -- Merge branch '75-attribute-to-distinguish-a-user-logged-in-via-kypo-or-muni-oidc-provider' into 'master'
|\  
| * 01603e9 -- Resolve "Attribute to distinguish a user logged in via KYPO or MUNI oidc provider"
|/  
* d8d3997 -- Update pom.xml version based on GitLab tag. Done by CI
*   926b272 -- Merge branch '74-changed-ssl-protocol-in-rest-template-user-and-group' into 'master'
|\  
| * f08e19d -- Changed protocol for SSL
|/  
*   b5ef6c6 -- Merge branch '73-change-logging-from-info-to-debug-in-cunstomauthoritygranter-class-getauthorities-method' into 'master'
|\  
| * 75c2558 -- logger in getAuthorities() method changed from info to debug
|/  
* 739ea8c -- Change version listing
* d156b4d -- Update kypo2-user-and-group.properties issuer, e.g., address
* bc8b2bd -- Update pom.xml version based on GitLab tag. Done by CI
*   f8fdcd6 -- Merge branch '72-remove-maximum-sessions-configuration' into 'master'
|\  
| * 2831662 -- Removed configuration for maximum sessions
|/  
* c8a5d37 -- Update kypo2-user-and-group.properties
* 1d3a449 -- Update pom.xml version based on GitLab tag. Done by CI
* 89f18c6 -- Update postinst
* 580015f -- Update install
* 49341bc -- Update control
* 942ce9b -- Update service
* 86bfd37 -- Add configuration file to debian package.
* d65fbab -- Update README.md
* a4a3e91 -- Update kypo2-user-and-group.properties
* cf8d58a -- Update kypo2-user-and-group.properties
*   e36e51f -- Merge branch '70-running-multiple-oidc-providers-in-parallel' into 'master'
|\  
| * 762f68e -- Resolve "Running multiple OIDC providers in parallel"
|/  
* d20cac5 -- kypo2-user-and-group.properties added.
* bb10534 -- Update README.md
* 13121b3 -- Do not restart systemd service
* 6af935a -- Fix postinst script
* e2c9fc9 -- Update pom.xml version based on GitLab tag. Done by CI
*   342b1bb -- Merge branch '71-make-deb-packages-lighter' into 'master'
|\  
| * 5cf3bff -- Do not upload Deb package as artifact
| * 3786cec -- Do not upload Deb package as artifact
| * 03d7e14 -- Install from jars, edit service file
|/  
*   2c0e13a -- Merge branch '69-expiration-date-cannot-be-edited' into 'master'
|\  
| * 50e7f44 -- A change of expiration date will take effect.
|/  
*   3b15874 -- Merge branch '68-enable-https-in-springboot-project' into 'master'
|\  
| * a169547 -- Resolve "Enable HTTPS in SpringBoot project"
|/  
* b167b3d -- Create Deb package in deployDebian stage
* 8375e3f -- update path to run spring boot project
*   bcfca85 -- Merge branch '67-get-users-with-given-logins-fails-when-list-of-logins-is-empty' into 'master'
|\  
| * e9835f5 -- getUsersWithGivenLogins now doesnt crash on empty set
|/  
*   0cdbf01 -- Merge branch '66-integrate-aop-aspects-into-project' into 'master'
|\  
| * 9d702d1 -- Resolve "Integrate AOP aspects into project"
|/  
* d810063 -- Do not export Deb package as a GitLab CI artifact
* e8538f4 -- Fix debian/install file
* 0c941b2 -- Edit debian/rules
*   957cf0c -- Merge branch '64-complete-documentation-in-whole-project' into 'master'
|\  
| * 8e605d1 -- Documentation of project completed
|/  
*   a0bf923 -- Merge branch '65-upgradae-versions-of-dependencies' into 'master'
|\  
| * 9eae42d -- Resolve "Upgradae versions of dependencies"
|/  
*   c76511c -- Merge branch '63-fix-possibility-to-remove-itself-from-administrator-group' into 'master'
|\  
| * 96b5818 -- Modified query for getting users no in given group and admin cannot remove himself from admin group
|/  
*   702a38b -- Merge branch '62-redesign-administrator-group-the-administrator-is-not-able-to-delete-himself-from-that-group' into 'master'
|\  
| * 7fb2f04 -- Resolve "Redesign Administrator Group. The administrator is not Able to Delete Himself From That Group"
|/  
*   f63f493 -- Merge branch '61-endpoint-for-getting-users-by-given-logins' into 'master'
|\  
| * 62150d7 -- New endpoint for getting users by given logins
|/  
* a0226a0 -- Repaired equal, hashCode and toString methods in model and DTO classes
*   5b46f69 -- Merge branch '37-create-integration-tests' into 'master'
|\  
| * 1e128fe -- Resolve "Create integration tests"
|/  
*   3754f39 -- Merge branch '58-optimze-sql-selects-in-project' into 'master'
|\  
| * 9820635 -- Optimalization of methods and SQL queries
|/  
* 837142d -- add version to the final .jar.
*   2909158 -- Merge branch '57-create-scheduler-for-removing-groups-after-expiration-date' into 'master'
|\  
| * c15941f -- Scheduler for removing expired groups from database
|/  
*   6f36e07 -- Merge branch '56-change-role-description-to-be-optional' into 'master'
|\  
| * 09b2874 -- Role description changed to optional
|/  
* 672d5df -- Names of main groups changed in service and facade layer
*   662aae1 -- Merge branch '55-change-names-of-main-groups' into 'master'
|\  
| * a23785b -- Changed default names of main groups
|/  
*   e72da7a -- Merge branch '54-add-role-description' into 'master'
|\  
| * a31ba61 -- Resolve "Add role description"
|/  
*   f2ae3c0 -- Merge branch '53-add-expiration-date-to-groups' into 'master'
|\  
| * a2f54c8 -- Resolve "Add expiration date to groups"
|/  
* 2b5f326 -- change version to 1.
* 45e53db -- Add author tags to ResourceServerSecurityConfig class.
* 4c587e0 -- add version to the user-and-group.
*   9e2d14e -- Merge branch '52-add-first-name-and-family-name-to-the-database' into 'master'
|\  
| * 6fcbdfe -- Given name and family name added to the user.
|/  
* 3c5fbd5 -- Add author tags to Java classes.
*   634f0d5 -- Merge branch '47-add-service-file-to-debianization' into 'master'
|\  
| * 918ba87 -- Remove property file
| * 5717f9d -- Cleaning up the code
| * 84f537a -- Fix typo
| * fdd3cc3 -- Pull master before Deb package
| * 9ac9750 -- Add properties file
| * 1f2b463 -- Add if check
| * 7f8576e -- Remove openjdk11 dependency
| * 00b2bce -- Automatically link newer jars
| * b7c4dd2 -- Merge two phases into one
| * 32e782c -- Use different Docker image
| * 88e9acf -- Fix build name
| * a641366 -- Use jars from previous stage
| * fc4a9dd -- Run mvn package within tests
| * fd27689 -- Edit jar paths
* |   09c454d -- Merge branch '50-reduce-the-number-of-queries-by-hibernate-in-roles-queries' into 'master'
|\ \  
| * | 3092ecb -- Microservice is fetched in JOIN FETCH.
|/ /  
* |   53ad0b3 -- Merge branch '49-reduce-number-of-queries-retrieved-in-groups-queries' into 'master'
|\ \  
| * | 70bc0bc -- Group queries merged to single query.
|/ /  
* |   59a04dc -- Merge branch '48-reduce-the-number-of-queries-from-hibernate' into 'master'
|\ \  
| |/  
|/|   
| * a296670 -- Resolve "Reduce the number of queries from Hibernate"
|/  
* f832cb6 -- change release tag to source and target.
* 09569bb -- Update pom.xml version based on GitLab tag. Done by CI
* 5ce2462 -- remove flyway core dependency and stay only with flyway plugin.
*   d1420b3 -- Merge branch '46-refactor-module-names-to-fit-the-kypo2-training-logic-and-to-fix-the-ci' into 'master'
|\  
| * c6b3efd -- module names refactored.
|/  
* 40a476d -- Rollback to older version
* 8616c15 -- Rollback to older version
* 077d3dc -- Update pom.xml version based on GitLab tag. Done by CI
*   000e394 -- Merge branch 'master' of gitlab.ics.muni.cz:kypo2/services-and-portlets/kypo2-user-and-group
|\  
| * a3f04fe -- project.version attribute in properties removes.
* | 6b693b5 -- Rollback to version 1.0.0, edit CI
|/  
* cbd98e4 -- Update pom.xml version based on GitLab tag. Done by CI
*   63b0fbd -- Merge branch '27-debianize-this-project' into 'master'
|\  
| * 0839aca -- Add the new CI/CD phase
* |   a2bd533 -- Merge branch '44-change-created-index-to-unique-index-for-unique-columns' into 'master'
|\ \  
| * | d56e759 -- change some indexes to unique indexes.
|/ /  
* |   a7e606f -- Merge branch '43-add-indexes-on-suitable-columns' into 'master'
|\ \  
| * | 9fb4454 -- Resolve "Add indexes on suitable columns."
|/ /  
* |   ceaec9d -- Merge branch '42-user-cannot-be-deleted-from-default-group' into 'master'
|\ \  
| * | b8b1ba4 -- User cannto be removed from default group.
|/ /  
* |   e0360a4 -- Merge branch '27-debianize-this-project' into 'master'
|\ \  
| |/  
| * ece1c65 -- Debianize the project + enable CI
*   76a9818 -- Merge branch '39-new-created-group-should-not-have-guest-role' into 'master'
|\  
| * ac73ae2 -- New group does not have guest role
|/  
*   f9b07bc -- Merge branch '40-create-conflict-exception-to-handler-in-rest-layer' into 'master'
|\  
| * 44fcfad -- Conflict exception added to handler, change name from APIError to ApiError
|/  
*   dfa00a9 -- Merge branch '41-name-of-main-group-cannot-be-changed' into 'master'
|\  
| * 4cb0d33 -- Cannot change name of main group
|/  
*   405b7fb -- Merge branch '38-users-are-not-added-to-default-group' into 'master'
|\  
| * c950429 -- Users are added to groups during start of application
|/  
*   f144a73 -- Merge branch '36-could-not-remove-users-from-group' into 'master'
|\  
| * 043ca37 -- Users can be deleted from group
|/  
*   ad34ad8 -- Merge branch '34-during-start-all-users-from-groups-are-deleted' into 'master'
|\  
| * a8101f1 -- Users stay in groups after restart of application
|/  
*   f439a82 -- Merge branch '33-change-sercurity-for-getroleofusermethod' into 'master'
|\  
| * 224353d -- Rights for method get role of user changed
|/  
* 4180ea3 -- Change password to postgres.
*   30139a8 -- Merge branch '32-repair-endpoint-for-getting-users-of-given-role' into 'master'
|\  
| * 91ced0b -- Added new endpoint for getting users by given role type
|/  
*   df1b751 -- Merge branch '31-change-descriptions-of-delete-group-s-endpoints' into 'master'
|\  
| * d505748 -- Changed description of endpoints for deleting groups
|/  
*   e4bdb13 -- Merge branch '30-add-values-to-can_be_delete-field-in-groupdto' into 'master'
|\  
| * 213b97a -- GroupDTO return can be deleted: true only for main groups
|/  
*   9e66127 -- Merge branch '29-create-tests-for-new-methods' into 'master'
|\  
| * 1f7e595 -- Resolve "Create tests for new methods"
|/  
*   7f7a494 -- Merge branch '28-edit-config-files-and-readme-on-gitlab' into 'master'
|\  
| * 3e46e08 -- Readme edited.
|/  
* 775c422 -- Merge branch '26-managing-all-roles-from-microservices-in-one-microservice' into 'master'
* bb1ee46 -- Resolve "Managing all roles from microservices in one microservice"