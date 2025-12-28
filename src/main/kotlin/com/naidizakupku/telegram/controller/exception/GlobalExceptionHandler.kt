package com.naidizakupku.telegram.controller.exception

import com.naidizakupku.telegram.service.CodeServiceException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant
import java.util.*

/**
 * Глобальный обработчик исключений для REST API
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Invalid value")
        }
        
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Validation error: traceId=$traceId, errors=$errors")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Ошибка валидации входных данных",
                traceId = traceId,
                details = errors
            ))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }
        
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Constraint violation: traceId=$traceId, errors=$errors")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Constraint Violation",
                message = "Нарушение ограничений валидации",
                traceId = traceId,
                details = errors
            ))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Type mismatch: traceId=$traceId, parameter=${ex.name}, value=${ex.value}")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Type Mismatch",
                message = "Некорректный тип параметра: ${ex.name}",
                traceId = traceId
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Illegal argument: traceId=$traceId, message=${ex.message}")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Некорректный запрос",
                traceId = traceId
            ))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Message not readable: traceId=$traceId, message=${ex.message}")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Malformed Request",
                message = "Некорректный формат запроса",
                traceId = traceId
            ))
    }

    @ExceptionHandler(CodeServiceException::class)
    fun handleCodeServiceException(ex: CodeServiceException): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.error("Code service error: traceId=$traceId, message=${ex.message}", ex)
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Code Service Error",
                message = ex.message ?: "Ошибка сервиса кодов",
                traceId = traceId
            ))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.warn("Resource not found: traceId=$traceId, message=${ex.message}")
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Ресурс не найден",
                traceId = traceId
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get("traceId") ?: "unknown"
        logger.error("Unexpected error: traceId=$traceId", ex)
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                timestamp = Instant.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "Внутренняя ошибка сервера",
                traceId = traceId
            ))
    }
}

/**
 * Стандартизированный формат ответа об ошибке
 */
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val traceId: String,
    val details: Map<String, String>? = null
)

