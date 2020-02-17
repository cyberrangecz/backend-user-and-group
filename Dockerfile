# Build stage
FROM maven:3.6.2-jdk-11-slim AS build

COPY ./ /app
#COPY ./etc/settings.xml /root/.m2/settings.xml # in case you need to set your proprietary Nexus or other repository
WORKDIR /app
RUN mvn clean install -DskipTests
RUN apt-get update && apt-get install -y supervisor postgresql

# default arguments that should be overridden during start of the container
ENV USERNAME=postgres
ENV PASSWORD=postgres
ENV POSTGRES_DB=user-and-group

WORKDIR /app/kypo2-persistence-user-and-group
RUN /etc/init.d/postgresql start &&\
    su postgres -c "createdb -O \"$USERNAME\" $POSTGRES_DB" &&\
    su postgres -c "psql -c \"ALTER USER $USERNAME PASSWORD '$PASSWORD';\""

RUN /etc/init.d/postgresql start &&\
    mvn flyway:migrate -Djdbc.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" -Djdbc.username="$USERNAME" -Djdbc.password="$PASSWORD" &&\
    mkdir -p /var/log/supervisor &&\
    cp /app/supervisord.conf /etc/supervisor/supervisord.conf &&\
    cp /app/kypo2-rest-user-and-group/target/kypo2-rest-user-and-group-*.jar /app/kypo-rest-user-and-group.jar

WORKDIR /app
EXPOSE 8084
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]