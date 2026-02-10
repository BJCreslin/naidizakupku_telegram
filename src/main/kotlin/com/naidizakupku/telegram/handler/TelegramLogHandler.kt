package com.naidizakupku.telegram.handler

import com.naidizakupku.telegram.service.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.ByteArrayInputStream

/**
 * Обработчик команды /log для отправки лог-файла в Telegram
 */
@Component
class TelegramLogHandler(
    private val logService: LogService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TelegramLogHandler::class.java)
        private const val DEFAULT_LINES_COUNT = 1000
    }
    
    /**
     * Обрабатывает команду /log
     * @param update обновление от Telegram
     * @return SendDocument для отправки файла или SendMessage для текстового ответа
     */
    fun handleLogCommand(update: Update): Any {
        val chatId = update.message.chatId.toString()
        val user = update.message.from
        val text = update.message.text
        
        try {
            logger.info("Обработка команды /log для пользователя ${user.id}")
            
            // Парсим аргументы команды
            val args = text.split(" ").drop(1)
            val linesCount = parseLinesCount(args)
            
            // Проверяем, существует ли лог-файл
            if (!logService.isLogFileExists()) {
                val errorMessage = SendMessage().apply {
                    this.chatId = chatId
                    this.text = """
                        ❌ <b>Лог-файл не найден</b>
                        
                        Путь: ${logService.getLogFilePath()}
                        
                        Убедитесь, что приложение настроено для записи логов.
                    """.trimIndent()
                    this.parseMode = "HTML"
                }
                return errorMessage
            }
            
            // Получаем лог-файл как массив байтов
            val logBytes = logService.getLogFileAsBytes(linesCount)
            
            // Если лог пустой или содержит только ошибку
            val logContent = String(logBytes, Charsets.UTF_8)
            if (logContent.startsWith("❌")) {
                val errorMessage = SendMessage().apply {
                    this.chatId = chatId
                    this.text = logContent
                    this.parseMode = "HTML"
                }
                return errorMessage
            }
            
            // Создаем имя файла с датой
            val fileName = "application_${getCurrentTimestamp()}.log"
            
            // Создаем InputFile из массива байтов
            val inputFile = InputFile(ByteArrayInputStream(logBytes), fileName)
            
            // Создаем документ для отправки
            val document = SendDocument().apply {
                this.chatId = chatId
                this.document = inputFile
                this.caption = buildCaption(linesCount)
                this.parseMode = "HTML"
            }
            
            logger.info("Отправка лог-файла пользователю ${user.id}, размер: ${logBytes.size} байт")
            return document
            
        } catch (e: Exception) {
            logger.error("Ошибка при обработке команды /log для пользователя ${user.id}", e)
            
            return SendMessage().apply {
                this.chatId = chatId
                this.text = """
                    ❌ <b>Произошла ошибка при получении лог-файла</b>
                    
                    ${e.message}
                """.trimIndent()
                this.parseMode = "HTML"
            }
        }
    }
    
    /**
     * Обрабатывает команду /loginfo для получения информации о лог-файле
     * @param update обновление от Telegram
     * @return SendMessage с информацией о лог-файле
     */
    fun handleLogInfoCommand(update: Update): SendMessage {
        val chatId = update.message.chatId.toString()
        val user = update.message.from
        
        try {
            logger.info("Обработка команды /loginfo для пользователя ${user.id}")
            
            val info = logService.getLogFileInfo()
            
            return SendMessage().apply {
                this.chatId = chatId
                this.text = info
                this.parseMode = "HTML"
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка при обработке команды /loginfo для пользователя ${user.id}", e)
            
            return SendMessage().apply {
                this.chatId = chatId
                this.text = """
                    ❌ <b>Произошла ошибка при получении информации о лог-файле</b>
                    
                    ${e.message}
                """.trimIndent()
                this.parseMode = "HTML"
            }
        }
    }
    
    /**
     * Парсит количество строк из аргументов команды
     * @param args список аргументов
     * @return количество строк
     */
    private fun parseLinesCount(args: List<String>): Int {
        return if (args.isNotEmpty()) {
            try {
                val count = args[0].toInt()
                if (count > 0 && count <= 10000) count else DEFAULT_LINES_COUNT
            } catch (e: NumberFormatException) {
                DEFAULT_LINES_COUNT
            }
        } else {
            DEFAULT_LINES_COUNT
        }
    }
    
    /**
     * Создает подпись к файлу
     * @param linesCount количество строк в файле
     * @return подпись в формате HTML
     */
    private fun buildCaption(linesCount: Int): String {
        return """
            📄 <b>Лог-файл приложения</b>
            
            📊 Строк: $linesCount
            📅 Дата: ${getCurrentTimestamp()}
            
            <i>Последние $linesCount строк из лог-файла</i>
        """.trimIndent()
    }
    
    /**
     * Получает текущую метку времени в формате yyyy-MM-dd_HH-mm-ss
     * @return строка с меткой времени
     */
    private fun getCurrentTimestamp(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        return java.time.LocalDateTime.now().format(formatter)
    }
}
