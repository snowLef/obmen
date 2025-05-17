pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'obmen'
    }

    stages {
        stage('Checkout') {
            steps {
                git credentialsId: '7777a270-e4fe-4838-849d-9357d40c539f', url: 'https://github.com/snowLef/obmen.git'
            }
        }

        stage('Build JAR') {
            steps {
                sh './mvnw clean package -DskipTests' // или './gradlew build'
            }
        }

        stage('Build & Run with Docker Compose') {
            steps {
                sh 'docker-compose down --remove-orphans'
                sh 'docker-compose up -d --build'
            }
        }

        stage('Wait for App Startup') {
            steps {
                script {
                    echo "Ожидание запуска приложения..."
                    sleep 20 // при необходимости можно заменить на healthcheck
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def status = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health", returnStdout: true).trim()
                    if (status != "200") {
                        error("Приложение не запустилось успешно. HTTP статус: ${status}")
                    } else {
                        echo "Приложение успешно работает (200 OK)"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Останавливаем docker-compose...'
            sh 'docker-compose down'
        }
    }
}
