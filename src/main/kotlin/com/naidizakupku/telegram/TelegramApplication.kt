package com.naidizakupku.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Главный класс Spring Boot приложения
 */
@SpringBootApplication
class TelegramApplication

fun main(args: Array<String>) {
    runApplication<TelegramApplication>(*args)
}

