pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_PATH = "./docker-compose.yml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/snowLef/obmen.git'
            }
        }

        stage('Build JAR') {
            steps {
                bat './gradlew clean build'
            }
        }

        stage('Build & Run with Docker Compose') {
            steps {
                bat "docker-compose -f %DOCKER_COMPOSE_PATH% up -d --build"
            }
        }

        stage('Wait for App Startup') {
            steps {
                bat 'timeout /t 10'
            }
        }

        stage('Health Check') {
            steps {
                bat 'curl http://localhost:8080/actuator/health'
            }
        }
    }

    post {
        always {
            echo 'Останавливаем docker-compose...'
            bat "docker-compose -f %DOCKER_COMPOSE_PATH% down"
        }
    }
}
