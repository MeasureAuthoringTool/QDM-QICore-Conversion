FROM amazoncorretto:21-al2-jdk
ARG JAR_FILE=target/mapping-services-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar

RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar \
    && curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.yml

ENTRYPOINT ["java","-javaagent:newrelic.jar","-jar","/app.jar"]
