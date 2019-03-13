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
        stage('bedrock-core') {
            agent {
              label 'linux'
            }
            steps {
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -am -pl bedrock-core clean install'
                }
            }
        }
        stage('bedrock-runtime-maven-tests') {
            agent {
              label 'linux'
            }
            steps {
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -am -pl bedrock-runtime-maven-tests clean install'
                }
            }
        }
    }
}
