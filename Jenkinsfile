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
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/mat-fhir-services ."
          //sh "sudo docker tag ${ECR_REPO}/mat-fhir-services:latest ${ECR_URL}/${ECR_REPO}/mat-fhir-services:latest"
          //sh "sudo docker push ${ECR_URL}/${ECR_REPO}/mat-fhir-services:latest"
        }
      }   
    }
    stage('Push cql-elm-translation to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        dir("cql-elm-translation") {
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/cql-elm-translation ."
          //sh "sudo docker tag ${ECR_REPO}/cql-elm-translation:latest ${ECR_URL}/${ECR_REPO}/cql-elm-translation:latest"
          //sh "sudo docker push ${ECR_URL}/${ECR_REPO}/cql-elm-translation:latest"
        }
      }   
    } 
    stage('Push qdm-qicore-mapping-services to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        dir("qdm-qicore-mapping-services") {
          sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/qdm-qicore-mapping-services ."
          // sh "sudo docker tag ${ECR_REPO}/qdm-qicore-mapping-services:latest ${ECR_URL}/${ECR_REPO}/qdm-qicore-mapping-services:latest"
          // sh "sudo docker push ${ECR_URL}/${ECR_REPO}/qdm-qicore-mapping-services:latest"
        }
      }   
    }
  }
}