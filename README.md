# QDM-QICore-Conversion
This project provides restful services to convert clinical quality measures authored with CMS developed Measure Authoring Tool (MAT) converting them
to Fast Healthcare Interoperable Resources(FHIR) release R4.  It utilizes MAT mysql instance as the datasource while reaching out to external systems 
such as NLMs VSAC to gather valueSets needed for measure evaluation.

## Requirements
1.  Java 1.8
2.  Maven 3.3.9 or higher
2.  MySQL 5.7.x
3.  Local or remote accessibility to MAT database installed in MySQL
4.  User login to NLM VSAC system via UMLS.
5.  Hapi-Fhir Jpaserver deployed in Tomcat and accessible locally or remotely.  You can clone and build HAPI-FHIR JPAServer at
https://github.com/MeasureAuthoringTool/mat-fhir-jpaserver/tree/HapiFhir3.7-R4/hapi-fhir-jpaserver-starter.  Follow the instructions 
in the README.md file.
6.  MongoDB version 3.4.23 or greater.

## Setting Up Your Local Development and Test Environment
1.  Checkout this project

```
$ git clone https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion.git
```

2.  Navigate to project module parent directory

```
$ cd QDM-QICore-Conversion/qdm-qicore-parent
```

3.  Build the project
```
$ mvn clean install
```
or to skip testing
```
$ mvn clean install -DskipTests
```

## Configure for Running as Micro-service Locally
1. Using vi, your IDE, or some other editor modify the mat-fhir-services project 'application.yaml' and 'application-local.yaml files they can be found QDM-QICore-Conversion/mat-fhir-services/src/main/resources.

application.yaml - `Note:` profile is configured for local
```
server:
  port: 9080


spring:
  profiles:
    active: local
  jpa:
    open-in-view: false

vsac-client:
  server: https://vsac.nlm.nih.gov/vsac/ws/Ticket
  service: http://umlsks.nlm.nih.gov
  retrieve-multi-oids-service: https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?
  profile-service: https://vsac.nlm.nih.gov/vsac/profiles
  version-service: https://vsac.nlm.nih.gov/vsac/oid/
  vsac-server-drc-url: https://vsac.nlm.nih.gov/vsac
  use-cache: true
  cache-directory: /opt/vsac/cache

measures:
  allowed:
    versions: v5.5,v5.6,v5.7,v5.8
```

application-local.yaml
```
spring:
  datasource:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mat?serverTimezone=UTC&max_allowed_packet=16777216
    username: mat
    password: mat
  jpa:
    hibernate.ddl-auto: none
    generate-ddl: false
    show-sql: false
  data:
    mongodb:
      database: mat_conversion_results
      uri: mongodb://localhost

logging:
  level:
    root: INFO
    gov.cms.mat: DEBUG
    org.hl7.fhir.r4.hapi.ctx: WARN
    org.exolab: WARN

fhir:
  r4:
    baseurl: http://localhost:8080/hapi-fhir-jpaserver/fhir/

qdmqicore:
  conversion:
    baseurl: http://localhost:9090

cql:
  conversion:
    baseurl: http://localhost:7070
```

Note:  You will most likely only need to change the datasource username and password.

2. Save your changes.

3. Update cql-elm-translation project application.yaml and application-local.yaml files which can be found at
QDM-QICore-Conversion/cql-elm-translation/src/main/resources directory

application.yaml - Note: the profile is set to local
```
server:
  port: 7070

spring:
  profiles:
    active: local
```

application-local.yaml
```
fhir:
  conversion:
    baseurl: http://localhost:9080
```

4. Save you changes.


3. Update your .bash_profile file to include

```
export VSAC_USER={username}
export VSAC_PASS={password}
```

5. Determine if you have write permissions to /opt directory, if not create the vsac cache directory /opt/vsac/cache

6. Build the project again after theses changes.

## Configure for Running in Tomcat Locally
1. Using vi, your IDE, or some other editor modify the mat-fhir-services project 'application.yaml' and 'application-tomcat-local.yaml files they can be found QDM-QICore-Conversion/mat-fhir-services/src/main/resources.

application.yaml - `Note:` profile is configured for tomcat-local
```
server:
  port: 9080


spring:
  profiles:
    active: tomcat-local
  jpa:
    open-in-view: false

vsac-client:
  server: https://vsac.nlm.nih.gov/vsac/ws/Ticket
  service: http://umlsks.nlm.nih.gov
  retrieve-multi-oids-service: https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?
  profile-service: https://vsac.nlm.nih.gov/vsac/profiles
  version-service: https://vsac.nlm.nih.gov/vsac/oid/
  vsac-server-drc-url: https://vsac.nlm.nih.gov/vsac
  use-cache: true
  cache-directory: /opt/vsac/cache

measures:
  allowed:
    versions: v5.5,v5.6,v5.7,v5.8
```

application-tomcat-local.yaml
```
spring:
  datasource:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mat?serverTimezone=UTC&max_allowed_packet=16777216
    username: mat
    password: mat
  jpa:
    hibernate.ddl-auto: none
    generate-ddl: false
    show-sql: false
  data:
    mongodb:
      database: mat_conversion_results
      uri: mongodb://localhost

logging:
  level:
    root: INFO
    gov.cms.mat: DEBUG
    org.hl7.fhir.r4.hapi.ctx: WARN
    org.exolab: WARN

fhir:
  r4:
    baseurl: http://localhost:8080/hapi-fhir-jpaserver/fhir/

qdmqicore:
  conversion:
    baseurl: http://localhost:8080/qdm-qicore-mapping-services-0.0.1-SNAPSHOT/

cql:
  conversion:
    baseurl: http://localhost:8080/cql-elm-translation-0.0.1-SNAPSHOT/
```

Note:  You will most likely only need to change the datasource username and password.

2. Save your changes.

3. Update cql-elm-translation project application.yaml and application-tomcat-local.yaml files which can be found at
QDM-QICore-Conversion/cql-elm-translation/src/main/resources directory

application.yaml - Note: the profile is set to tomcat-local
```
server:
  port: 7070

spring:
  profiles:
    active: tomcat-local
```

application-tomcat-local.yaml
```
fhir:
  conversion:
    baseurl: http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/
```

4. Save you changes.


3. Update your .bash_profile file to include

```
export VSAC_USER={username}
export VSAC_PASS={password}
```
You will need to restart tomcat so it see's these values.

5. Determine if you have write permissions to /opt directory, if not create the vsac cache directory /opt/vsac/cache

6. Build the project again after theses changes.

## Creating Profiles for Other environments.
You can create profiles for varying deployment scenarios, tomcat-dev, tomcat-qa tomcat-prod, or microservice-dev, etc by sourcing the existing examples in the mat-fhir-services and cql-elm-translation projects.  The profile file must begin with `application` and use the separator `-`, example `application-tomcat-dev.yaml` would describe the tomcat-dev profile.  You then must set the profile in the application.yaml file.  
```
spring:
  profiles:
    active: tomcat-dev
```
Rebuild the project.

## Deployment Considerations
These microservices are not intended to be publically exposed on internet, they are used (consumed) by CMS MAT and in future Bonnie applications.  For testing purpose only `mat-fhir-services` should be exposed, but not in pre-production or production deployments.  Some things to consider,
1.  These services should be co-located on same host or vm, `mat-fhir-services` will call `qdm-qicore-mapping-services` and `cql-elm-translation` services.
2.  mongodb should be deployed on same host or vm.
3.  Can be co-located with HAPI-FHIR resource server.
4.  MAT and MAT mysql database does not need to be co-located on same host or vm.
5.  HAPI-FHIR resource server mysql database does not need to be co-located on same host or vm.

## Running the project Locally As Micro-Service
Note: Both mat-fhir-services and cql-elm-translation project profiles should built with  be `profile` set to `local`.
1.  Navigate to the MAT-FHIR-Services directory

```
$ cd QDM-QICore-Conversion/mat-fhir-services
```

2. Launch the micro service

```
$ mvn spring-boot:run 
```

3. Navigate to the QDM-QICORE-Mapping-Services directory

```
$ cd ../qdm-qicore-mapping-services
```

4. Launch the micro service

```
$ mvn spring-boot:run
```

5. Navigate to CQL-ELM-Translation directory
```
$ cd ../cql-elm-translation
```

6. Launch the micro service
```
$ mvn spring-boot:run
```

## Deploying and Running Inside Tomcat
Note: Both mat-fhir-services and cql-elm-translation project profiles should built with  be `profile` set to `tomcat-local`.

1. Copy the the following project war files to  <tomcat-installation>/webapps directory for autodeployment
  
  QDM-QICore-Conversion/mat-fhir-services/target/mat-fhir-services-0.0.1-SNAPSHOT.war
  
  QDM-QICore-Conversion/qdm-qicore-mapping-services/target/qdm-qicore-mapping-services-0.0.1-SNAPSHOT.war
  
  QDM-QICore-Conversion/cql-elm-translation/target/cql-elm-translation-0.0.1-SNAPSHOT.war

## Viewing API and Testing Via Swagger During Development
Swagger provides a mechanism to view(and test) available service endpoints, their input criteria, and results.  You can
access this at;

When running as micro-service,
```
http://localhost:9080/swagger-ui.html
```
When running in Tomcat container,
```
http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/swagger-ui/index.html?url=/mat-fhir-services-0.0.1-SNAPSHOT/v3/api-docs&validatorUrl=
```


## FHIR Validation of Measure

Request URL example

When running as micro-service
```
http://localhost:9080/orchestration/measure?id=40280382649c54c30164d76256dd11dc&conversionType=VALIDATION&xmlSource=MEASURE
```
When running in Tomcat container,
```
http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/orchestration/measure?id=40280382649c54c30164d76256dd11dc&conversionType=VALIDATION&xmlSource=MEASURE
```


Note: You may also access the Orchestration-Controller API using swagger at http://localhost:9080/swagger-ui.html.

![FHIR validation flow](https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion/blob/develop/FHIR%20Validation.png)

## FHIR Validation and Conversion of Measure

Request URL example

When running as micro-service
```
http://localhost:9080/orchestration/measure?id=40280382649c54c30164d76256dd11dc&conversionType=CONVERSION&xmlSource=MEASURE
```
When running in Tomcat container,
```
http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/orchestration/measure?id=40280382649c54c30164d76256dd11dc&conversionType=CONVERSION&xmlSource=MEASURE
```

Note: You may also access the Orchestration-Controller API using swagger at http://localhost:9080/swagger-ui.html.

![FHIR validation and conversion flow](https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion/blob/develop/FHIR%20Validation%20and%20Conversion.png)

## Accessing Validation and Conversion Error Reports

Request URL example

When running as micro-service
```
http://localhost:9080/report/find?measureId=40280382649c54c30164d76256dd11dc
```
When running in Tomcat container,
```
http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/report/find?measureId=40280382649c54c30164d76256dd11dc
```

Note: You may also access the TranslationReport-Controller API using swagger at http://localhost:9080/swagger-ui.html.


## Searching for FHIR Resources - Some Basics
The HAPI-FHIR UI, http://localhost:8080/hapi-fhir-jpaserver/ will provide you with examples of how it is querying the system.  For additional information refer to documentation at https://hapifhir.io.

**Programmatically**
```
// Create a client (only needed once)
FhirContext ctx = FhirContext.forR4();
IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/hapi-fhir-jpaserver/fhir");
```

```
// Invoke the client and perform Measure search
Bundle bundle = client.search().forResource(Measure.class)
.prettyPrint()
.execute();
```

**Using URL**
In the form of GET http://localhost:8080/hapi-fhir-jpaserver/fhir/Measure/{mat uuid}

```
GET http://localhost:8080/hapi-fhir-jpaserver/fhir/Measure/402803826529d99f0165d33515622e23/

Set Accept = application/xml or application/json depending on your preference.
```
