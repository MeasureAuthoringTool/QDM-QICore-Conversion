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

## Configuring Your Environment
1. Using vi, your IDE, or some other editor modify the project configuration file 'application.yaml'.

```
server:
  port: 9080

spring:
  datasource:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mat?serverTimezone=UTC
    username: mat
    password: mat
  jpa:
    hibernate.ddl-auto: none
    generate-ddl: false
    show-sql: false

logging:
  level:
    root: INFO
    gov.cms.mat: DEBUG

vsac-client:
  server: https://vsac.nlm.nih.gov/vsac/ws/Ticket
  service: http://umlsks.nlm.nih.gov
  retrieve-multi-oids-service: https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?
  profile-service: https://vsac.nlm.nih.gov/vsac/profiles
  version-service: https://vsac.nlm.nih.gov/vsac/oid/
  vsac-server-drc-url: https://vsac.nlm.nih.gov/vsac
  use-cache: true
  cache-directory: /opt/vsac/cache

fhir:
  r4:
    baseurl: http://localhost:8080/hapi-fhir-jpaserver/fhir/
```

Note:  You will most likely only need to change the datasource username and password.

2. Save your changes.

3. Update your .bash_profile file to include

```
export VSAC_USER={username}
export VSAC_PASS={password}
```

5. Determine if you have write permissions to /opt directory, if not create the vsac cache directory /opt/vsac/cache

6. Build the project again after theses changes.


## Running the project
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

## Viewing API and Testing Via Swagger
Swagger provides a mechanism to view(and test) available service endpoints, their input criteria, and results.  You can
access this at;

```
http://localhost:9080/swagger-ui.html
```

## FHIR Validation of Measure

![FHIR validation flow](FHIR Validation.png)

## FHIR Validation and Conversion of Measure

![FHIR validation and conversion flow](https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion/blob/develop/FHIR%20Validation%20and%20Conversion.png)


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
