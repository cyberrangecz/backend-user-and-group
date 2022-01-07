############ BUILD STAGE ############
FROM maven:3.6.2-jdk-11-slim AS build
WORKDIR /app
ARG PROJECT_ARTIFACT_ID=kypo-rest-user-and-group
# Default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO
COPY pom.xml /app/pom.xml
COPY kypo-api-user-and-group /app/kypo-api-user-and-group
COPY kypo-persistence-user-and-group /app/kypo-persistence-user-and-group
COPY kypo-service-user-and-group /app/kypo-service-user-and-group
COPY kypo-security-user-and-group /app/kypo-security-user-and-group
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID
# Build JAR file
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-user-and-group.jar

############ RUNNABLE STAGE ############
FROM openjdk:11-jre-slim AS runnable
WORKDIR /app
COPY /etc/initial-users.yml /app/etc/initial-users.yml
COPY /etc/user-and-group.properties /app/etc/user-and-group.properties
COPY entrypoint.sh /app/entrypoint.sh
COPY --from=build /app/kypo-rest-user-and-group.jar ./
RUN apt-get update && \
    # Required to use nc command in the wait for it function, see entrypoint.sh
    apt-get install -y netcat && \
    # Make a file executable
    chmod a+x entrypoint.sh
EXPOSE 8084
ENTRYPOINT ["./entrypoint.sh"]
