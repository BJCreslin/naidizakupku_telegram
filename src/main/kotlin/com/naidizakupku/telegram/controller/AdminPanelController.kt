package com.naidizakupku.telegram.controller

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер для обработки SPA routing админки
 * Возвращает index.html для всех маршрутов админки, которые не являются файлами
 */
@RestController
@RequestMapping("/admin")
class AdminPanelController(
    private val resourceLoader: ResourceLoader
) {
    private val logger = LoggerFactory.getLogger(AdminPanelController::class.java)

    /**
     * Обработка всех маршрутов админки для поддержки SPA routing
     * Возвращает index.html для всех запросов, которые не являются статическими файлами
     * 
     * Примечание: Spring Boot сначала пытается найти статический ресурс,
     * и только если не находит, запрос попадает в этот контроллер
     */
    @GetMapping(
        value = ["/**"]
    )
    fun adminPanel(request: HttpServletRequest): ResponseEntity<Resource> {
        val requestPath = request.requestURI.removePrefix("/admin")
        logger.debug("Admin panel request: {}", request.requestURI)
        
        // Если путь содержит точку (расширение файла), это статический файл
        // Пытаемся найти файл в разных местах (для совместимости со старыми сборками)
        if (requestPath.contains(".") && !requestPath.endsWith(".html")) {
            // Пытаемся найти файл в assets/js/ или assets/css/
            val fileName = requestPath.substringAfterLast("/")
            val possiblePaths = listOf(
                "classpath:/static/admin/assets/js/$fileName",
                "classpath:/static/admin/assets/css/$fileName",
                "classpath:/static/admin/assets/$fileName",
                "classpath:/static/admin/$fileName"
            )
            
            logger.debug("Looking for static file: {} in paths: {}", fileName, possiblePaths)
            
            for (path in possiblePaths) {
                try {
                    val resource = resourceLoader.getResource(path)
                    if (resource.exists() && resource.isReadable) {
                        logger.debug("Found file at: {}", path)
                        val contentType = when {
                            fileName.endsWith(".js") -> "application/javascript"
                            fileName.endsWith(".css") -> "text/css"
                            fileName.endsWith(".woff") -> "font/woff"
                            fileName.endsWith(".woff2") -> "font/woff2"
                            fileName.endsWith(".ttf") -> "font/ttf"
                            fileName.endsWith(".eot") -> "application/vnd.ms-fontobject"
                            else -> MediaType.APPLICATION_OCTET_STREAM_VALUE
                        }
                        return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                            .body(resource)
                    }
                } catch (e: Exception) {
                    logger.debug("Error checking path {}: {}", path, e.message)
                }
            }
            
            // Файл не найден, возвращаем 404
            logger.warn("Static file not found: {} (requested path: {})", fileName, requestPath)
            return ResponseEntity.notFound().build()
        }
        
        // Для всех остальных путей возвращаем index.html (SPA routing)
        val resource = ClassPathResource("/static/admin/index.html")
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            .body(resource)
    }
}

