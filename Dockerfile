#
# Build stage
#
FROM maven:3.6.2-jdk-11-slim AS build

COPY ./ /app
COPY ./etc/settings.xml /root/.m2/settings.xml
WORKDIR /app
RUN mvn clean package

#
# Package stage
#
FROM openjdk:11-jdk AS jdk
COPY --from=build /app/kypo2-rest-user-and-group/target/kypo2-rest-user-and-group-*.jar /app/kypo-rest-user-and-group.jar
COPY --from=build /app/etc/user-and-group.properties /app/etc/user-and-group.properties
COPY --from=build /app/etc/initial-users.yml /app/etc/initial-users.yml
WORKDIR /app
EXPOSE 8084
ENTRYPOINT ["java", "-Dpath.to.config.file=/app/etc/user-and-group.properties", "-Dpath.to.initial.users=/app/etc/initial-users.yml", "-jar", "/app/kypo-rest-user-and-group.jar"]
