#!groovy
node("master") {
    properties([[$class: 'BuildDiscarderProperty', 
                 strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', 
                            artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10']]])
    
    def mvnHome = tool 'mvn'
    def mvnJdk7Image = "orientdb/mvn-gradle-zulu-jdk-7"

    stage('Source checkout') {

        checkout scm
    }

    stage('Run tests on Java7') {
        docker.image("${mvnJdk7Image}").inside("--memory=4g ${env.VOLUMES}") {
            try {

                sh "${mvnHome}/bin/mvn  --batch-mode -V -U  clean deploy -Dsurefire.useFile=false"

                slackSend(color: '#00FF00', message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")

            } catch (e) {
                currentBuild.result = 'FAILURE'
                slackSend(channel: '#jenkins-failures', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})\n${e}")
                throw e;
            } finally {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
            }

        }
    }
}



