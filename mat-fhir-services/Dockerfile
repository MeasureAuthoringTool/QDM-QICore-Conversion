FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/mat-fhir-services-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]