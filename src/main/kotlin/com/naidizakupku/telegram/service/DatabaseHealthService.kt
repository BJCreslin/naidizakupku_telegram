package com.naidizakupku.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class DatabaseHealthService {
    
    private val logger = LoggerFactory.getLogger(DatabaseHealthService::class.java)
    
    @Autowired
    private lateinit var dataSource: DataSource
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    fun checkDatabaseConnection(): Boolean {
        return try {
            val connection = dataSource.connection
            val isValid = connection.isValid(5)
            connection.close()
            
            if (isValid) {
                logger.info("✅ Подключение к БД успешно")
                checkTables()
                true
            } else {
                logger.error("❌ Подключение к БД недействительно")
                false
            }
        } catch (e: Exception) {
            logger.error("❌ Ошибка подключения к БД: ${e.message}")
            false
        }
    }
    
    private fun checkTables() {
        try {
            val tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String::class.java
            )
            
            logger.info("📋 Найденные таблицы: ${tables.joinToString(", ")}")
            
            if (tables.contains("users")) {
                val userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long::class.java)
                logger.info("👥 Количество пользователей: $userCount")
            } else {
                logger.warn("⚠️  Таблица 'users' не найдена!")
            }
            
            // Проверяем таблицу Liquibase
            if (tables.contains("databasechangelog")) {
                val changeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM databasechangelog", Long::class.java)
                logger.info("🔄 Количество примененных изменений Liquibase: $changeCount")
            } else {
                logger.warn("⚠️  Таблица 'databasechangelog' не найдена!")
            }
            
        } catch (e: Exception) {
            logger.error("❌ Ошибка при проверке таблиц: ${e.message}")
        }
    }
}
