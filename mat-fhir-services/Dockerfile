FROM openjdk:11-jdk
RUN mkdir -p /opt/fhir/distribution

RUN curl -O https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem
RUN curl -O https://s3.amazonaws.com/rds-downloads/rds-ca-2019-us-east-1.pem
RUN keytool -import -keystore $JAVA_HOME/lib/security/cacerts -trustcacerts -storepass changeit -alias "AWSrdsRootCACert" -file rds-ca-2019-root.pem --noprompt
RUN keytool -import -keystore $JAVA_HOME/lib/security/cacerts -trustcacerts -storepass changeit -alias "AWSrdsIntCACert" -file rds-ca-2019-us-east-1.pem --noprompt

ARG JAR_FILE=target/mat-fhir-services-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
