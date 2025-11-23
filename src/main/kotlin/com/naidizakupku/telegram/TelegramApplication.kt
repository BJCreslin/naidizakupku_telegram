package com.naidizakupku.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Главный класс Spring Boot приложения
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
class TelegramApplication

fun main(args: Array<String>) {
    runApplication<TelegramApplication>(*args)
}

