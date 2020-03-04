# Build stage
FROM maven:3.6.2-jdk-11-slim AS build

## default environment variables for database settings
ARG USERNAME=postgres
ARG PASSWORD=postgres
ARG POSTGRES_DB=user-and-group
ARG PROJECT_ARTIFACT_ID=kypo2-rest-user-and-group

## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=https://YOUR-PATH-TO-PROPRIETARY_REPO/repository/maven-public/

COPY ./ /app
WORKDIR /app
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL
RUN apt-get update && apt-get install -y supervisor postgresql

WORKDIR /app/kypo2-persistence-user-and-group
RUN /etc/init.d/postgresql start &&\
    su postgres -c "createdb -O \"$USERNAME\" $POSTGRES_DB" &&\
    su postgres -c "psql -c \"ALTER USER $USERNAME PASSWORD '$PASSWORD';\""

RUN /etc/init.d/postgresql start &&\
    mvn flyway:migrate -Djdbc.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" -Djdbc.username="$USERNAME" -Djdbc.password="$PASSWORD" &&\
    mkdir -p /var/log/supervisor &&\
    cp /app/supervisord.conf /etc/supervisor/supervisord.conf &&\
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/$PROJECT_ARTIFACT_ID.jar

WORKDIR /app
EXPOSE 8084
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]