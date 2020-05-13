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