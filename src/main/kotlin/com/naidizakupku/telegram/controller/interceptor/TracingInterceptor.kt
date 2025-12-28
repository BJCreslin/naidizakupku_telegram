package com.naidizakupku.telegram.controller.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

/**
 * Перехватчик для автоматической обработки заголовков
 */
@Component
class TracingInterceptor : HandlerInterceptor {

    companion object {
        /**
         *  уникальный идентификатор всего запроса
         */
        const val TRACE_ID_HEADER = "X-Trace-Id"

        /**
         * идентификатор текущего участка обработки
         */
        const val SPAN_ID_HEADER = "X-Span-Id"

        /**
         * общий идентификатор запроса пользователя
         */
        const val REQUEST_ID_HEADER = "X-Request-Id"

        /**
         * для корреляции связанных запросов
         */
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val PARENT_SPAN_ID_HEADER = "X-Parent-Span-Id"

        const val TRACE_ID_MDC = "traceId"
        const val SPAN_ID_MDC = "spanId"
        const val REQUEST_ID_MDC = "requestId"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {

        // Получаем или генерируем trace ID
        val traceId = request.getHeader(TRACE_ID_HEADER)
            ?: generateUUID()

        // Получаем или генерируем span ID
        val spanId = request.getHeader(SPAN_ID_HEADER)
            ?: generateUUID()

        // Получаем или генерируем request ID
        val requestId = request.getHeader(REQUEST_ID_HEADER)
            ?: traceId // Используем trace ID как request ID если не передан

        // Добавляем в MDC для логирования
        MDC.put(TRACE_ID_MDC, traceId)
        MDC.put(SPAN_ID_MDC, spanId)
        MDC.put(REQUEST_ID_MDC, requestId)

        // Добавляем в response headers для клиента
        response.setHeader(TRACE_ID_HEADER, traceId)
        response.setHeader(SPAN_ID_HEADER, spanId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        // Сохраняем в request attributes для использования в контроллере
        request.setAttribute(
            "traceContext", TraceContextData(
                traceId = traceId,
                spanId = spanId,
                requestId = requestId,
                parentSpanId = request.getHeader(PARENT_SPAN_ID_HEADER),
                correlationId = request.getHeader(CORRELATION_ID_HEADER)
            )
        )

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // Очищаем MDC после завершения запроса
        MDC.clear()
    }

    private fun generateUUID(): String = UUID.randomUUID().toString()
}

data class TraceContextData(
    val traceId: String,
    val spanId: String,
    val requestId: String,
    val parentSpanId: String? = null,
    val correlationId: String? = null
)

@Component
class WebConfig(
    private val tracingInterceptor: TracingInterceptor,
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // Rate limiting должен быть первым
        registry.addInterceptor(rateLimitInterceptor)
        registry.addInterceptor(tracingInterceptor)
    }
}