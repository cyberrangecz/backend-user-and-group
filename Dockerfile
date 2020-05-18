# Build stage
FROM maven:3.6.2-jdk-11-slim AS build

## default environment variables for database settings
ARG USERNAME=postgres
ARG PASSWORD=postgres
ARG POSTGRES_DB=user-and-group
ARG PROJECT_ARTIFACT_ID=kypo2-rest-user-and-group

## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO

# install
RUN apt-get update && apt-get install -y supervisor postgresql

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
COPY pom.xml /app/pom.xml
COPY kypo2-api-user-and-group /app/kypo2-api-user-and-group
COPY kypo2-persistence-user-and-group /app/kypo2-persistence-user-and-group
COPY kypo2-service-user-and-group /app/kypo2-service-user-and-group
COPY kypo2-security-user-and-group /app/kypo2-security-user-and-group
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID


# build uag
RUN cd /app && \
    mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-user-and-group.jar && \
    cd /app/kypo2-persistence-user-and-group && \
    /etc/init.d/postgresql start && \
    mvn flyway:migrate -Djdbc.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" -Djdbc.username="$USERNAME" -Djdbc.password="$PASSWORD" && \
    /etc/init.d/postgresql stop

WORKDIR /app
EXPOSE 8084
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/app/supervisord.conf"]
