pipeline {
  agent any

  stages {
    stage('Install dependencies') {
      steps { 
        sh "mvn install:install-file -Dfile=./lib/vsac-1.0.jar -DgroupId=mat -DartifactId=vsac -Dversion=1.0 -Dpackaging=jar"
      }
    }
    stage('Build MAT mat-fhir-services services') {
      steps { 
        dir("mat-fhir-commons") {
          sh "mvn clean install -DskipTests"   
        }
        dir("mat-server-commons") {
          sh "mvn clean install -DskipTests"   
        }
        dir("mat-rest-commons") {
          sh "mvn clean install -DskipTests"   
        }
        dir("vsac-mat-conversion") {
          sh "mvn clean install -DskipTests"   
        }
        dir("mat-fhir-services") {
          sh "mvn clean compile package -DskipTests"   
        }
      }
    } 
    stage('Build MAT cql-elm-translation services') {
      steps {
        sh "echo 'Copy cql-elm-translation YAML for tomcat-dev use'"
        sh "cp ${WORKSPACE}/cql-elm-translation/src/main/resources/application-tomcat-local.yaml ${WORKSPACE}/cql-elm-translation/src/main/resources/application-tomcat-dev.yaml"
        sh label: 'Changes to the YAML file', script: 'sed -ri \'s/^(\\s*)(active\\s*:\\s*local\\s*$)/\\1active: tomcat-dev/\' ${WORKSPACE}/cql-elm-translation/src/main/resources/application.yaml'
        sh label: 'Changes to the YAML file', script: 'sed -ri \'s|^(\\s*)(baseurl\\s*:\\s*http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/\\s*$)|\\1baseurl: http://internal-mat-dev-ecs-lb-1195232407.us-east-1.elb.amazonaws.com/mat-fhir-services/|\' ${WORKSPACE}/cql-elm-translation/src/main/resources/application-tomcat-dev.yaml'
        dir("mat-rest-commons") {
          sh "mvn clean install -DskipTests"   
        }
        dir("cql-elm-translation") {
          sh "mvn clean compile package -DskipTests"
        }
      }
    } 
    stage('Build MAT qdm-qicore-mapping-services services') {
      steps {
        dir("qdm-qicore-mapping-services") {
          sh "mvn clean compile package -DskipTests"
        }
      }
    }
    stage('Push mat-fhir-services to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        dir("mat-fhir-services") {
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}mat-fhir-services-ecr ."
          sh "sudo docker tag ${ECR_REPO}mat-fhir-services-ecr:latest ${ECR_URL}/${ECR_REPO}mat-fhir-services-ecr:latest"
          sh "sudo docker push ${ECR_URL}/${ECR_REPO}mat-fhir-services-ecr:latest"
        }
      }   
    }
    stage('Push cql-elm-translation to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        dir("cql-elm-translation") {
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}cql-elm-translation-ecr ."
          sh "sudo docker tag ${ECR_REPO}cql-elm-translation-ecr:latest ${ECR_URL}/${ECR_REPO}cql-elm-translation-ecr:latest"
          sh "sudo docker push ${ECR_URL}/${ECR_REPO}cql-elm-translation-ecr:latest"
        }
      }   
    } 
    stage('Push qdm-qicore-mapping-services to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        dir("qdm-qicore-mapping-services") {
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}qdm-qicore-mapping-services-ecr ."
          sh "sudo docker tag ${ECR_REPO}qdm-qicore-mapping-services-ecr:latest ${ECR_URL}/${ECR_REPO}qdm-qicore-mapping-services-ecr:latest"
          sh "sudo docker push ${ECR_URL}/${ECR_REPO}qdm-qicore-mapping-services-ecr:latest"
        }
      }   
    }
  }
}