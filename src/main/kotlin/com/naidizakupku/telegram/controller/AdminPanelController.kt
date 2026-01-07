package com.naidizakupku.telegram.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
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
class AdminPanelController {

    /**
     * Обработка всех маршрутов админки для поддержки SPA routing
     * Возвращает index.html для всех запросов, которые не являются статическими файлами
     * 
     * Примечание: Spring Boot сначала пытается найти статический ресурс,
     * и только если не находит, запрос попадает в этот контроллер
     */
    @GetMapping(
        value = ["/**"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun adminPanel(request: HttpServletRequest): ResponseEntity<Resource> {
        val requestPath = request.requestURI.removePrefix("/admin")
        
        // Если путь содержит точку (расширение файла), это статический файл
        // В этом случае Spring Boot должен был обработать его через ResourceHandler
        // Если запрос дошел сюда, значит файл не найден, возвращаем index.html для SPA routing
        if (requestPath.contains(".") && !requestPath.endsWith(".html")) {
            // Это файл с расширением (не HTML), возвращаем 404
            return ResponseEntity.notFound().build()
        }
        
        // Для всех остальных путей возвращаем index.html (SPA routing)
        val resource = ClassPathResource("/static/admin/index.html")
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            .body(resource)
    }
}

