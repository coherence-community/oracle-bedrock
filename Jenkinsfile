pipeline {
    agent none
    stages {
        stage('compile') {
            agent {
              label 'linux'
            }
            steps {
                echo 'Compile'
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn -DskipTests clean install'
                }
            }
        }
        stage('maven-tests') {
            agent {
              label 'linux'
            }
            steps {
                echo 'Maven Tests'
                withMaven(jdk: 'Jdk8', maven: 'Maven3.6.0', mavenSettingsConfig: 'maven-settings', tempBinDir: '') {
                   sh 'mvn dependency:get -Dartifact=org.apache.maven.plugins:maven-downloader-plugin:jar:1.0'
                   sh 'mvn dependency:get -Dartifact=org.hamcrest:hamcrest-library:jar:1.3'
                   sh 'mvn -am -pl bedrock-runtime-maven-tests clean install'
                }
            }
        }
    }
}
