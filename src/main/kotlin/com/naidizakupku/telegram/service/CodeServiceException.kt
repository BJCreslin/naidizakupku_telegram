package com.naidizakupku.telegram.service

/**
 * Исключение для ошибок сервиса работы с кодами
 */
class CodeServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

