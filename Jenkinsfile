pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
    }

    tools {
        maven 'Maven 3.9.9'
        jdk 'jdk21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                // Запускаем сборку Maven
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Run with Docker Compose') {
            steps {
                bat 'docker-compose -f %COMPOSE_FILE% up -d --build'
            }
        }

        stage('Wait for App Startup') {
            steps {
                echo 'Waiting for app to start...'
                sleep time: 15, unit: 'SECONDS'
            }
        }
}
    }

    post {
        always {
            echo 'Останавливаем docker-compose...'
            bat 'docker-compose -f %COMPOSE_FILE% down'
        }
    }
}
