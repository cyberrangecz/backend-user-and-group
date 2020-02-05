#
# Build stage
#
FROM maven:3.6.2-jdk-11-slim AS build

COPY ./ /app
COPY ./etc/settings.xml /root/.m2/settings.xml
WORKDIR /app
RUN mvn clean package
RUN apt-get update && apt-get install -y supervisor postgresql


WORKDIR /app/kypo2-persistence-user-and-group
RUN /etc/init.d/postgresql start &&\
    su -c "createdb -O postgres user-and-group" postgres &&\
    su postgres -c "psql -c \"ALTER USER postgres PASSWORD 'postgres';\""

RUN /etc/init.d/postgresql start &&\
    mvn flyway:migrate -Djdbc.url="jdbc:postgresql://localhost:5432/user-and-group" -Djdbc.username="postgres" -Djdbc.password="postgres" &&\
    mkdir -p /var/log/supervisor &&\
    cp /app/supervisord.conf /etc/supervisor/supervisord.conf &&\
    cp /app/kypo2-rest-user-and-group/target/kypo2-rest-user-and-group-*.jar /app/kypo-rest-user-and-group.jar

WORKDIR /app
EXPOSE 8084
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]