FROM openjdk:11-jdk
ARG JAR_FILE=target/cql-elm-translation-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]