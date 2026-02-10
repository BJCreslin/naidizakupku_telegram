package com.naidizakupku.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Сервис для работы с лог-файлами приложения
 */
@Service
class LogService(
    @Value("\${logging.file.name:./logs/application.log}")
    private val logFilePath: String
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(LogService::class.java)
        private const val MAX_LOG_SIZE_BYTES = 50 * 1024 * 1024 // 50 MB максимальный размер для отправки
        private const val DEFAULT_LINES_COUNT = 1000 // Количество строк по умолчанию
    }
    
    /**
     * Получает последние N строк из лог-файла
     * @param linesCount количество строк (по умолчанию 1000)
     * @return список строк лога
     */
    fun getLastLines(linesCount: Int = DEFAULT_LINES_COUNT): List<String> {
        return try {
            val logFile = getLogFile()
            if (!logFile.exists()) {
                logger.warn("Лог-файл не найден: ${logFile.absolutePath}")
                return listOf("❌ Лог-файл не найден: ${logFile.absolutePath}")
            }
            
            val allLines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8)
            val lines = allLines.takeLast(linesCount)
            
            logger.info("Получено ${lines.size} строк из лог-файла")
            lines
        } catch (e: Exception) {
            logger.error("Ошибка при чтении лог-файла", e)
            listOf("❌ Ошибка при чтении лог-файла: ${e.message}")
        }
    }
    
    /**
     * Получает логи за последние N минут
     * @param minutes количество минут (по умолчанию 60)
     * @return список строк лога за указанный период
     */
    fun getRecentLogs(minutes: Int = 60): List<String> {
        return try {
            val logFile = getLogFile()
            if (!logFile.exists()) {
                logger.warn("Лог-файл не найден: ${logFile.absolutePath}")
                return listOf("❌ Лог-файл не найден: ${logFile.absolutePath}")
            }
            
            val cutoffTime = LocalDateTime.now().minusMinutes(minutes.toLong())
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            
            val allLines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8)
            val filteredLines = allLines.filter { line ->
                try {
                    val timestampStr = line.substringBefore(" [")
                    val timestamp = LocalDateTime.parse(timestampStr, formatter)
                    timestamp.isAfter(cutoffTime)
                } catch (e: Exception) {
                    false
                }
            }
            
            logger.info("Получено ${filteredLines.size} строк за последние $minutes минут")
            filteredLines
        } catch (e: Exception) {
            logger.error("Ошибка при чтении лог-файла", e)
            listOf("❌ Ошибка при чтении лог-файла: ${e.message}")
        }
    }
    
    /**
     * Получает лог-файл как массив байтов для отправки в Telegram
     * @param linesCount количество строк (по умолчанию 1000)
     * @return массив байтов лог-файла
     */
    fun getLogFileAsBytes(linesCount: Int = DEFAULT_LINES_COUNT): ByteArray {
        return try {
            val logFile = getLogFile()
            if (!logFile.exists()) {
                logger.warn("Лог-файл не найден: ${logFile.absolutePath}")
                return "❌ Лог-файл не найден: ${logFile.absolutePath}".toByteArray(StandardCharsets.UTF_8)
            }
            
            val lines = getLastLines(linesCount)
            val content = lines.joinToString("\n")
            
            // Проверяем размер файла
            val bytes = content.toByteArray(StandardCharsets.UTF_8)
            if (bytes.size > MAX_LOG_SIZE_BYTES) {
                logger.warn("Лог-файл слишком большой (${bytes.size} байт), обрезаем")
                val truncatedContent = content.take(MAX_LOG_SIZE_BYTES)
                truncatedContent.toByteArray(StandardCharsets.UTF_8)
            } else {
                bytes
            }
        } catch (e: Exception) {
            logger.error("Ошибка при чтении лог-файла", e)
            "❌ Ошибка при чтении лог-файла: ${e.message}".toByteArray(StandardCharsets.UTF_8)
        }
    }
    
    /**
     * Получает информацию о лог-файле
     * @return информация о файле в виде строки
     */
    fun getLogFileInfo(): String {
        return try {
            val logFile = getLogFile()
            if (!logFile.exists()) {
                return "❌ Лог-файл не найден: ${logFile.absolutePath}"
            }
            
            val sizeKB = logFile.length() / 1024
            val lastModified = java.io.File(logFile.toURI()).lastModified()
            val lastModifiedTime = java.time.Instant.ofEpochMilli(lastModified)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
            
            val linesCount = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8).size
            
            """
            📄 <b>Информация о лог-файле</b>
            
            📁 Путь: ${logFile.absolutePath}
            📏 Размер: $sizeKB KB
            📊 Строк: $linesCount
            🕐 Последнее изменение: $lastModifiedTime
            """.trimIndent()
        } catch (e: Exception) {
            logger.error("Ошибка при получении информации о лог-файле", e)
            "❌ Ошибка при получении информации о лог-файле: ${e.message}"
        }
    }
    
    /**
     * Получает путь к лог-файлу
     * @return путь к лог-файлу
     */
    fun getLogFilePath(): String {
        return logFilePath
    }
    
    /**
     * Проверяет, существует ли лог-файл
     * @return true, если файл существует
     */
    fun isLogFileExists(): Boolean {
        return try {
            getLogFile().exists()
        } catch (e: Exception) {
            logger.error("Ошибка при проверке существования лог-файла", e)
            false
        }
    }
    
    /**
     * Получает объект File для лог-файла
     * @return объект File
     */
    private fun getLogFile(): File {
        val path = Paths.get(logFilePath)
        return path.toFile()
    }
}
