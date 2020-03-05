FROM openjdk:11-jdk
ARG JAR_FILE=target/hapi-fhir-jpaserver-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
