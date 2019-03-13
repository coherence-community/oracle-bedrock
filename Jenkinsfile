pipeline {
    agent none
    stages {
        stage('maven-build') {
            agent {
              label 'linux'
            }
            steps {
                echo 'Maven Build'
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -DskipTests clean install'
                }
            }
        }
    }
}
