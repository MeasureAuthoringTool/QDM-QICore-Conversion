FROM openjdk:16.0.2-jdk
ARG JAR_FILE=target/mapping-services-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip \
    && unzip newrelic-java.zip \
    && sed -i "/common: &default_settings/a \ \ host: gov-collector.newrelic.com" newrelic/newrelic.yml
ENTRYPOINT ["java","-javaagent:newrelic/newrelic.jar","-jar","/app.jar"]
