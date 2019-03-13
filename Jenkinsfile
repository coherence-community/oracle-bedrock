pipeline {
    agent none
    stages {
        stage('compile') {
            agent {
              label 'linux'
            }
            steps {
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -DskipTests clean install'
                }
            }
        }
        stage('bedrock-without-vagrant-or-docker') {
            agent {
              label 'linux'
            }
            steps {
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -amd -pl \!bedrock-runtime-docker-tests,\!bedrock-runtimevagrant-tests clean install'
                }
            }
        }
    }
}
