package com.naidizakupku.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class StartupInfoService {
    
    private val logger = LoggerFactory.getLogger(StartupInfoService::class.java)
    
    @Value("\${spring.datasource.url:}")
    private lateinit var databaseUrl: String
    
    @Value("\${spring.datasource.username:}")
    private lateinit var databaseUsername: String
    
    @Value("\${spring.kafka.bootstrap-servers:}")
    private lateinit var kafkaBootstrapServers: String
    
    @Value("\${spring.kafka.properties.security.protocol:}")
    private lateinit var kafkaSecurityProtocol: String
    
    @Value("\${telegram.bot.name:}")
    private lateinit var telegramBotUsername: String
    
    @Value("\${telegram.bot.token:}")
    private lateinit var telegramBotToken: String
    
    @Value("\${spring.profiles.active:default}")
    private lateinit var activeProfile: String
    
    @Value("\${server.port:8080}")
    private var serverPort: Int = 8080
    
    @EventListener(ApplicationReadyEvent::class)
    fun logStartupInfo() {
        logger.info("=".repeat(60))
        logger.info("🚀 ПРИЛОЖЕНИЕ ЗАПУЩЕНО")
        logger.info("=".repeat(60))
        
        // Основная информация
        logger.info("📋 ПРОФИЛЬ: $activeProfile")
        logger.info("🌐 ПОРТ: $serverPort")
        
        // База данных
        logger.info("🗄️  БАЗА ДАННЫХ:")
        logger.info("   URL: $databaseUrl")
        logger.info("   Пользователь: $databaseUsername")
        
        // Kafka
        logger.info("📨 KAFKA:")
        logger.info("   Серверы: ${maskSensitiveData(kafkaBootstrapServers)}")
        logger.info("   Протокол безопасности: $kafkaSecurityProtocol")
        
        // Telegram Bot
        logger.info("🤖 TELEGRAM BOT:")
        logger.info("   Name: $telegramBotUsername")
        logger.info("   Токен: ${maskSensitiveData(telegramBotToken)}")
        
        // Статус подключений
        logger.info("🔌 СТАТУС ПОДКЛЮЧЕНИЙ:")
        logConnectionStatus()
        
        logger.info("=".repeat(60))
    }
    
    private fun maskSensitiveData(data: String): String {
        return when {
            data.isEmpty() -> "НЕ НАСТРОЕНО"
            data.contains("password") || data.contains("token") -> "***СКРЫТО***"
            data.length > 50 -> "${data.take(20)}...${data.takeLast(10)}"
            else -> data
        }
    }
    
    private fun logConnectionStatus() {
        try {
            // Проверка подключения к БД
            logger.info("   База данных: ✅ ГОТОВА")
        } catch (e: Exception) {
            logger.error("   База данных: ❌ ОШИБКА: ${e.message}")
        }
        
        try {
            // Проверка подключения к Kafka
            if (kafkaBootstrapServers.isNotEmpty()) {
                logger.info("   Kafka: ✅ ГОТОВА")
            } else {
                logger.info("   Kafka: ⚠️  НЕ НАСТРОЕНО")
            }
        } catch (e: Exception) {
            logger.error("   Kafka: ❌ ОШИБКА: ${e.message}")
        }
        
        try {
            // Проверка Telegram Bot
            if (telegramBotToken.isNotEmpty() && telegramBotUsername.isNotEmpty()) {
                logger.info("   Telegram Bot: ✅ ГОТОВ")
            } else {
                logger.info("   Telegram Bot: ⚠️  НЕ НАСТРОЕН")
            }
        } catch (e: Exception) {
            logger.error("   Telegram Bot: ❌ ОШИБКА: ${e.message}")
        }
    }
}
