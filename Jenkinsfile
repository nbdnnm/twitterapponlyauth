pipeline {
    agent any
    parameters {
            string(name: 'authheader', defaultValue: '', description: 'Authentication header')
        }
    stages {
        stage('build') {
            steps {
                echo "${params.authheader}"
                bat "gradlew.bat -DauthHeader=${params.authheader} clean test allureReport"
            }
        }
    }
}