FROM amazoncorretto:17-al2-jdk
ARG JAR_FILE=target/mat-fhir-services-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /opt/fhir/distribution
RUN curl -O https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem \
    && curl -O https://s3.amazonaws.com/rds-downloads/rds-ca-2019-us-east-1.pem \
    && keytool -import -keystore $JAVA_HOME/lib/security/cacerts -trustcacerts -storepass changeit -alias "AWSrdsRootCACert" -file rds-ca-2019-root.pem --noprompt\
    && keytool -import -keystore $JAVA_HOME/lib/security/cacerts -trustcacerts -storepass changeit -alias "AWSrdsIntCACert" -file rds-ca-2019-us-east-1.pem --noprompt

RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar \
    && curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.yml

ENTRYPOINT ["java","-Dspring.profiles.active=docker","-javaagent:newrelic.jar","-jar","/app.jar"]

