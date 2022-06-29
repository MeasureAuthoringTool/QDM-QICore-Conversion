FROM openjdk:18.0-jdk
ARG JAR_FILE=target/cql-elm-translation-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar

RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar \
    && curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.yml

ENTRYPOINT ["java","-Dspring.profiles.active=docker","-javaagent:newrelic.jar","-jar","/app.jar"]
