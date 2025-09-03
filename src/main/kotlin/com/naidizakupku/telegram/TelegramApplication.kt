package com.naidizakupku.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Главный класс Spring Boot приложения
 */
@SpringBootApplication
@EnableScheduling
class TelegramApplication

fun main(args: Array<String>) {
    runApplication<TelegramApplication>(*args)
}

