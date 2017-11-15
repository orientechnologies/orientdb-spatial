#!groovy
node("master") {
        properties([[$class: 'BuildDiscarderProperty', 
                 strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', 
                            artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10']]])
    
        ansiColor('xterm') {

        def mvnHome = tool 'mvn'
        def mvnJdk8Image = "orientdb/mvn-gradle-zulu-jdk-8"

        stage('Source checkout') {

            checkout scm
        }

        stage('Run tests on Java8') {
            docker.image("${mvnJdk8Image}").inside("--memory=5g ${env.VOLUMES}") {
                try {

                    sh "${mvnHome}/bin/mvn  --batch-mode -V -U  clean deploy -Dmaven.test.failure.ignore=true -Dsurefire.useFile=false"
                    sh "${mvnHome}/bin/mvn  -f ./distribution/pom.xml --batch-mode  clean deploy -Pqa"

                    slackSend(color: '#00FF00', message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")

                } catch (e) {
                    currentBuild.result = 'FAILURE'
                    slackSend(channel: '#jenkins-failures', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
                    throw e;
                } finally {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
                }
            }
        }
    }
}



