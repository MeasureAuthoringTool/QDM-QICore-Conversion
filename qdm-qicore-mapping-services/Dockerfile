FROM openjdk:11-jdk
ARG JAR_FILE=target/qdm-qicore-mapping-services-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

