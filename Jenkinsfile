pipeline {
    agent any
    parameters {
            string(name: 'authheader', defaultValue: '', description: 'Authentication header')
        }
    stages {
        stage('build') {
            steps {
                bat "gradlew.bat -DauthHeader=${params.authheader} clean test allureReport"
            }
        }
    }
}