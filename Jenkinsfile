pipeline {
  agent {
    docker {
      image 'maven'
    }
    
  }
  stages {
    stage('Clean') {
      steps {
        sh 'mvn -f examples/mvc-security/pom.xml clean'
      }
    }
    stage('Compile') {
      steps {
        sh 'mvn -f examples/mvc-security/pom.xml compile'
      }
    }
  }
}