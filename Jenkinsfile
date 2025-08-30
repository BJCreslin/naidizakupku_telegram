pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'registry.gitflic.ru/project/bjcreslin/naidizakupku_telegram/telegram-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        REMOTE_HOST = 'your-ubuntu-server'
        REMOTE_USER = 'deploy'
        REMOTE_PATH = '/opt/telegram-app'
//         GITFLIC_USER = credentials('gitflic-registry').username
        GITFLIC_USER = "${env.GITFLIC_USER}"
        GITFLIC_PASS = "${env.GITFLIC_PASS}"
//         GITFLIC_PASS = credentials('gitflic-registry').password
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    // Собираем приложение с Gradle
                    sh './gradlew clean build'
                }
            }
            post {
                always {
                    // Сохраняем результаты сборки
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    // Запускаем тесты
                    sh './gradlew test'
                }
            }
            post {
                always {
                    // Публикуем результаты тестов
                    publishTestResults testResultsPattern: 'build/test-results/test/**/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Анализ кода с SonarQube
                    withSonarQubeEnv('SonarQube') {
                        sh './gradlew sonarqube'
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Собираем Docker образ
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.build("${DOCKER_IMAGE}:latest")
                }
            }
        }
        
        stage('Push Docker Image') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Логинимся в GitFlic registry
                    sh "echo ${GITFLIC_PASS} | docker login -u ${GITFLIC_USER} --password-stdin registry.gitflic.ru"
                    
                    // Пушим образы
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Деплоим на удаленный сервер
                    sshagent(['ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} '
                                # Останавливаем старый контейнер
                                docker stop telegram-app || true
                                docker rm telegram-app || true
                                
                                # Удаляем старый образ
                                docker rmi ${DOCKER_IMAGE}:latest || true
                                
                                # Логинимся в GitFlic registry
                                echo ${GITFLIC_PASS} | docker login -u ${GITFLIC_USER} --password-stdin registry.gitflic.ru
                                
                                # Скачиваем новый образ
                                docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}
                                docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                                
                                # Запускаем новый контейнер
                                docker run -d \\
                                    --name telegram-app \\
                                    --restart unless-stopped \\
                                    -p 8080:8080 \\
                                    -e POSTGRES_URL=${POSTGRES_URL} \\
                                    -e POSTGRES_USER=${POSTGRES_USER} \\
                                    -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \\
                                    -e KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS} \\
                                    -v ${REMOTE_PATH}/logs:/app/logs \\
                                    ${DOCKER_IMAGE}:latest
                                
                                # Проверяем здоровье приложения
                                sleep 30
                                curl -f http://localhost:8080/actuator/health || exit 1
                            '
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Очистка Docker образов
            sh 'docker system prune -f'
        }
        success {
            // Уведомление об успешном деплое
            emailext (
                subject: "Deploy Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} успешно задеплоен на ${REMOTE_HOST}",
                to: 'admin@example.com'
            )
        }
        failure {
            // Уведомление об ошибке
            emailext (
                subject: "Deploy Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} завершился с ошибкой. Проверьте логи Jenkins.",
                to: 'admin@example.com'
            )
        }
    }
}

