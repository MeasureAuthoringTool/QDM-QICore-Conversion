# QDM-QICore-Conversion
This project provides restful services to convert clinical quality measures authored with CMS developed Measure Authoring Tool (MAT) converting them
to Fast Healthcare Interoperable Resources(FHIR) release R4.  It utilizes MAT mysql instance as the datasource while reaching out to external systems 
such as NLMs VSAC to gather valueSets needed for measure evaluation.

## Requirements
1.  Java 15
2.  Maven 3.8.3 or higher
2.  MySQL 8.0.26 (Mat DB)
4.  User login to NLM VSAC system via UMLS
5.  (Optional) Docker v20 or higher

# Initial Setup

## Checking out ##
Checkout this project
```shell script
git clone https://github.com/MeasureAuthoringTool/QDM-QICore-Conversion.git
```
change to the develop branch.
```shell script
git checkout develop
```

```shell script
$ cd QDM-QICore-Conversion
```

### Important Security Setup With Git Secrets
- Use brew to install git secrets ```brew install git-secrets```
- Clone this repository (you can skip this if you've already cloned it from previous steps)
- Note: You may have to reinitialize these hooks each time you clone a new copy of the repo
- Follow these instructions for setting up the pre-commit hooks:
```
cd /path/to/QDM-QICore-Conversion
git secrets --install
git secrets --register-aws
```
Done! Now each commit should be automatically scanned for accidental AWS secret leaks.

### Env vars
Edit docker-env.sh and setup all the environment variables to match your environment.
Replace all the FIX_ME! entries with the appropriate information.
 
To load env vars:
```shell script
. ./docker-env.sh
```
You can also manually add a line to ~/.bash_profile to load it.

Run the following shell script to setup links for hapi-fhir libraries and value-sets.
```shell script
./create-links.sh
```

### Maven
Do a clean build of everything. This step is required to ensure Spring Boot can auto-configure the `BuildProperties` bean.
```shell script
mvn clean install
```
or to skip testing
```shell script
mvn clean install -DskipTests
```

### Docker Compose
Use the docker-compose-build.yml when you want to build containers from all the source code you just built.
```shell script
docker-compose pull
docker-compose build
docker-compose up
```

If you get an error running hapi-fhir-jpaserver that looks like this just ignore it its a known issue and not user impacting. 
```text
 Error executing DDL "create index IDX_VALUESET_C_DSGNTN_VAL on TRM_VALUESET_C_DESIGNATION (VAL)" via JDBC Statement
``` 


### Loading valuesets/codes
Run this to setup all the valuesets and codes needed on the hapi-fhir-server. 
You just need to run this once on a newly setup hapi-fhir-server.
curl -X GET "http://localhost:9080/library/find/load" -H "accept: */*"

### Urls
#### mat-fhir-services:
-  Actuator: 
   -  (local) http://localhost:9080/actuator
-  Swagger:  
   -  (local) http://localhost:9080/swagger

#### hapi-fhir-server:
-  Test Overlay: 
    - (local) http://localhost:6060/

#### qdm-qicore-mapping-services:
-  Actuator:  
   -  (local) http://localhost:9090/actuator
-  Swagger:   
   - (local) http://localhost:9090/swagger

#### cql-elm-translation:
-  Actuator: 
   - (local) http://localhost:7070/actuator
-  Swagger:  
   - (local) http://localhost:7070/swagger
