package com.naidizakupku.telegram.service.admin

import com.naidizakupku.telegram.domain.dto.admin.LogEntryDto
import com.naidizakupku.telegram.domain.dto.admin.PagedResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * Сервис для работы с логами
 */
@Service
class AdminLogService(
    @Value("\${logging.file.name:/app/logs/application.log}") private val logFilePath: String
) {

    private val logger = LoggerFactory.getLogger(AdminLogService::class.java)

    // Паттерн для парсинга логов Logback
    // Пример: 2024-01-01 15:30:00 [main] INFO  com.example.Service - Message
    private val logPattern = Pattern.compile(
        """(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})\s+\[([^\]]+)\]\s+(\w+)\s+([^\s]+)\s+-\s+(.*)"""
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Получить логи с пагинацией и фильтрами
     */
    fun getLogs(
        page: Int,
        size: Int,
        level: String? = null,
        traceId: String? = null,
        correlationId: String? = null
    ): PagedResponse<LogEntryDto> {
        val logFile = File(logFilePath)
        
        if (!logFile.exists() || !logFile.canRead()) {
            logger.warn("Log file not found or not readable: $logFilePath")
            return PagedResponse.of(emptyList(), page, size, 0)
        }

        val allLogs = parseLogFile(logFile)
        val filteredLogs = filterLogs(allLogs, level, traceId, correlationId)
        
        // Применяем пагинацию
        val start = page * size
        val end = minOf(start + size, filteredLogs.size)
        val pagedLogs = if (start < filteredLogs.size) {
            filteredLogs.subList(start, end)
        } else {
            emptyList()
        }

        return PagedResponse.of(
            content = pagedLogs,
            page = page,
            size = size,
            totalElements = filteredLogs.size.toLong()
        )
    }

    /**
     * Парсинг лог файла
     */
    private fun parseLogFile(logFile: File): List<LogEntryDto> {
        val logs = mutableListOf<LogEntryDto>()
        
        try {
            logFile.useLines { lines ->
                lines.forEach { line ->
                    parseLogLine(line)?.let { logs.add(it) }
                }
            }
        } catch (e: Exception) {
            logger.error("Error reading log file: ${e.message}", e)
        }

        // Возвращаем в обратном порядке (новые логи первыми)
        return logs.reversed()
    }

    /**
     * Парсинг одной строки лога
     */
    private fun parseLogLine(line: String): LogEntryDto? {
        val matcher = logPattern.matcher(line)
        
        if (!matcher.find()) {
            return null
        }

        val timestampStr = matcher.group(1)
        val thread = matcher.group(2)
        val level = matcher.group(3)
        val loggerName = matcher.group(4)
        val message = matcher.group(5)

        val timestamp = try {
            LocalDateTime.parse(timestampStr, dateFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }

        // Извлекаем traceId и correlationId из сообщения
        val traceId = extractTraceId(message)
        val correlationId = extractCorrelationId(message)
        val exception = extractException(line)

        return LogEntryDto(
            level = level,
            timestamp = timestamp,
            logger = loggerName,
            message = message,
            traceId = traceId,
            correlationId = correlationId,
            exception = exception
        )
    }

    /**
     * Фильтрация логов
     */
    private fun filterLogs(
        logs: List<LogEntryDto>,
        level: String?,
        traceId: String?,
        correlationId: String?
    ): List<LogEntryDto> {
        return logs.filter { log ->
            (level == null || log.level.equals(level, ignoreCase = true)) &&
            (traceId == null || log.traceId?.contains(traceId, ignoreCase = true) == true) &&
            (correlationId == null || log.correlationId?.contains(correlationId, ignoreCase = true) == true)
        }
    }

    /**
     * Извлечение traceId из сообщения
     */
    private fun extractTraceId(message: String): String? {
        val pattern = Pattern.compile("traceId[=:]\\s*([a-f0-9-]{36})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(message)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * Извлечение correlationId из сообщения
     */
    private fun extractCorrelationId(message: String): String? {
        val pattern = Pattern.compile("correlationId[=:]\\s*([a-f0-9-]{36})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(message)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * Извлечение информации об исключении
     */
    private fun extractException(line: String): String? {
        // Ищем строки, которые выглядят как stack trace
        if (line.contains("Exception") || line.contains("Error") || line.contains("at ")) {
            return line.trim()
        }
        return null
    }
}

