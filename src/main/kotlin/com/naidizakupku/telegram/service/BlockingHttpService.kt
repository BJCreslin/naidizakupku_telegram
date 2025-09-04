package com.naidizakupku.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

/***
 * WebClient в блокирующем режиме
 */
@Service
class BlockingHttpService(
    private val webClient: WebClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // GET запрос
    fun getData(url: String): String? {
        return try {
            webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .block() // Блокирующий вызов
        } catch (e: WebClientResponseException) {
            logger.error("Ошибка GET запроса: ${e.statusCode} - ${e.responseBodyAsString}")
            null
        }
    }

    // POST запрос с данными
    fun postData(url: String, data: Any): String? {
        return try {
            webClient
                .post()
                .uri(url)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (e: WebClientResponseException) {
            logger.error("Ошибка POST запроса: ${e.statusCode}")
            null
        }
    }

    // PUT запрос
    fun updateData(url: String, data: Any): Boolean {
        return try {
            webClient
                .put()
                .uri(url)
                .bodyValue(data)
                .retrieve()
                .toBodilessEntity()
                .block()
            true
        } catch (e: Exception) {
            logger.error("Ошибка PUT запроса", e)
            false
        }
    }

    // DELETE запрос
    fun deleteData(url: String): Boolean {
        return try {
            webClient
                .delete()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .block()
            true
        } catch (e: Exception) {
            logger.error("Ошибка DELETE запроса", e)
            false
        }
    }

    // Запрос с timeout
    fun getDataWithTimeout(url: String, timeoutSeconds: Long): String? {
        return try {
            webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .block(Duration.ofSeconds(timeoutSeconds))
        } catch (e: Exception) {
            logger.error("Timeout или ошибка запроса", e)
            null
        }
    }

    // Запрос с headers
    fun getWithHeaders(url: String, headers: Map<String, String>): String? {
        return try {
            val request = webClient.get().uri(url)

            headers.forEach { (key, value) ->
                request.header(key, value)
            }

            request
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        } catch (e: Exception) {
            logger.error("Ошибка запроса с headers", e)
            null
        }
    }

    // Обработка различных статус-кодов
    fun getWithStatusHandling(url: String): String? {
        return try {
            webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus({ status -> status.is4xxClientError }) { response ->
                    response.bodyToMono(String::class.java).map { body ->
                        RuntimeException("Client error: $body")
                    }
                }
                .onStatus({ status -> status.is5xxServerError }) { response ->
                    response.bodyToMono(String::class.java).map { body ->
                        RuntimeException("Server error: $body")
                    }
                }
                .bodyToMono(String::class.java)
                .block()
        } catch (e: Exception) {
            logger.error("Ошибка с обработкой статуса", e)
            null
        }
    }
}