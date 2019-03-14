pipeline {
    agent { any }
    parameters {
            string(name: 'authheader', defaultValue: '', description: 'Authentication header')
        }
    stages {
        stage('build') {
            steps {
                sh 'gradlew -DauthHeader=${params.authheader} clean test'
            }
        }
    }
}