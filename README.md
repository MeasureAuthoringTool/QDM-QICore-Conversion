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

'''
$ git clone https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion.git
'''

2.  Navigate to project module parent directory

'''
$ cd QDM-QICore-Conversion/qdm-qicore-parent
'''

3.  Build the project
'''
$ mvn clean install
'''
or to skip testing
'''
$ mvn clean install -DskipTests

## Configuring Your Environment
1. Using vi, your IDE, or some other editor modify the project configuration file 'application.yaml'.

'''
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
'''

Note:  You will most likely only need to change the datasource username and password.

2. Save your changes.

3. Update your .bash_profile file to include

'''
export VSAC_USER={username}
export VSAC_PASS={password}
'''

5. Determine if you have write permissions to /opt directory, if not create the vsac cache directory /opt/vsac/cache

6. Build the project again after theses changes.


## Running the project
1.  Navigate to the MAT-FHIR-Services directory

'''
$ cd QDM-QICore-Conversion/mat-fhir-services
'''

2. Launch the application

'''
$ mvn spring-boot:run
'''

## Converting to FHIR

### Measure Operations
Measure operations are constrained by the measure's QDM release version, **5.5 thru 5.8**, and the presence of **SIMPLE_XML** with the MEASURE_EXPORT table.
Translate All Measure - Translates all applicable measures.
Method: **GET** Endpoint: http://localhost:9080/qdmtofhir/translateAllMeasures

Translate All Measures Based on Measure Status - Translates all measure with a specific status.
Method: **GET** Endpoint: http://localhost:9080/qdmtofhir/translateMeasuresByStatus?measureStatus={measure_status}  
Currently MAT stores measure status as "In Progress" or "Complete".

Translate A Single Measure - Translates a specific measure based on it's MAT UUID.
Method: **GET** Endpoint: http://localhost:9080/qdmtofhir/translateMeasure?id={uuid}

Delete All Measures - Deletes all measures.
Method: **DELETE** Endpoint: http://localhost:9080/qdmtofhir/removeAllMeasures
**NOTE:** This operation is used for development and demonstration purposes.

### ValueSet Operations
ValueSet operations are constrained by the measure's QDM release version, **5.5 thru 5.8**.

Translate All ValueSets:  Translate all applicable valueSets.
Method: **GET** Endpoint: http://localhost:9080/valueSet/translateAll

Count All ValueSets:  Return count of all FHIR valueSet resources.
Method: **GET** Endpoint: http://localhost:9080/valueSet/count

Delete All ValueSets: Removes all valueSet resources.
Method: **DELETE** Endpoint: http://localhost:9080/valueSet/deleteAll.
**NOTE:** This operation is used for development and demonstration purposes.

### Library Operations
Creates FHIR Library resource from the Mat CQL_EXPORT table.  It is constrained by measures QDM release version **5.5 thru 5.8**

Translate All Libraries:  Translates all applicable libraries to FHIR Resource.
Method: **GET** Endpoint: http://localhost:9080/qdmtofhir/translateAllLibraries.

Delete All Libraries:  Deletes all loaded FHIR Library resources.
Method: **DELETE** Endpoint: http://localhost:9080/qdmtofhir/translateAllLibraries
**Note:** This operation is used for development and demonstration purposes.
