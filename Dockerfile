# Build stage
FROM maven:3.6.2-jdk-11-slim AS build

## default environment variables for database settings
ARG USERNAME=postgres
ARG PASSWORD=postgres
ARG POSTGRES_DB=user-and-group
ARG PROJECT_ARTIFACT_ID=kypo-rest-user-and-group

## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO

# install
RUN apt-get update && apt-get install -y supervisor postgresql netcat

# configure supervisor
RUN mkdir -p /var/log/supervisor

# configure postgres
RUN /etc/init.d/postgresql start && \
    su postgres -c "createdb -O \"$USERNAME\" $POSTGRES_DB" && \
    su postgres -c "psql -c \"ALTER USER $USERNAME PASSWORD '$PASSWORD';\"" && \
    /etc/init.d/postgresql stop

# copy only essential parts
COPY ["/etc/user-and-group.properties", "/etc/initial-users.yml", "/app/etc/"]
COPY supervisord.conf /app/supervisord.conf
COPY entrypoint.sh /app/entrypoint.sh
COPY pom.xml /app/pom.xml
COPY kypo-api-user-and-group /app/kypo-api-user-and-group
COPY kypo-persistence-user-and-group /app/kypo-persistence-user-and-group
COPY kypo-service-user-and-group /app/kypo-service-user-and-group
COPY kypo-security-user-and-group /app/kypo-security-user-and-group
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID

WORKDIR /app

# build uag
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-user-and-group.jar && \
    chmod a+x entrypoint.sh

EXPOSE 8084
ENTRYPOINT ["./entrypoint.sh"]
