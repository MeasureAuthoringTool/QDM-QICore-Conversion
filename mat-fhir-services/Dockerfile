FROM openjdk:11-jdk
RUN mkdir -p /opt/fhir/distribution
RUN mkdir -p /opt/vsac/cache
COPY ./cache  /opt/vsac/cache

ARG JAR_FILE=target/mat-fhir-services-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
