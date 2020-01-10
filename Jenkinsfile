pipeline {
  agent any

  stages {
/*     stage('Build MAT mat-fhir-services services') {
      steps { 
            sh "cd ${WORKSPACE}/mat-fhir-services"
            sh "mvn clean compile package -DskipTests"   
      }
    }
    stage('Build MAT cql-elm-translation services') {
      steps { 
            sh "cd ${WORKSPACE}/cql-elm-translation"
            sh "mvn clean compile package -DskipTests"   
      }
    } */
    stage('Build MAT qdm-qicore-mapping-services services') {
      steps { 
            sh "cd ${WORKSPACE}/qdm-qicore-mapping-services"
            sh "mvn clean compile package -DskipTests"   
      }
    }
/*     stage('Push mat-fhir-services to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        sh "cd ${WORKSPACE}/mat-fhir-services"
        sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/mat-fhir-services ."
        sh "sudo docker tag ${ECR_REPO}/mat-fhir-services:latest ${ECR_URL}/${ECR_REPO}/mat-fhir-services:latest"
        sh "sudo docker push ${ECR_URL}/${ECR_REPO}/mat-fhir-services:latest"
      }   
    }
    stage('Push cql-elm-translation to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        sh "cd ${WORKSPACE}/cql-elm-translation"
        sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/cql-elm-translation ."
        sh "sudo docker tag ${ECR_REPO}/cql-elm-translation:latest ${ECR_URL}/${ECR_REPO}/cql-elm-translation:latest"
        sh "sudo docker push ${ECR_URL}/${ECR_REPO}/cql-elm-translation:latest"
      }   
    } */
    stage('Push qdm-qicore-mapping-services to ECR') {
      steps {
        sh "sudo \$(aws ecr get-login --no-include-email --region us-east-1)"
        sh "cd ${WORKSPACE}/qdm-qicore-mapping-services"
        sh "sudo docker build --build-arg JAR_FILE=target/*.jar -t ${ECR_REPO}/qdm-qicore-mapping-services ."
        sh "sudo docker tag ${ECR_REPO}/qdm-qicore-mapping-services:latest ${ECR_URL}/${ECR_REPO}/qdm-qicore-mapping-services:latest"
        sh "sudo docker push ${ECR_URL}/${ECR_REPO}/qdm-qicore-mapping-services:latest"
      }   
    }
  }
}