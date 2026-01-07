package com.naidizakupku.telegram.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Конфигурация для раздачи статических ресурсов админки
 */
@Configuration
class WebMvcConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Раздача статических файлов админки из classpath:/static/admin/
        // Порядок важен: сначала обрабатываем assets, потом остальное
        registry.addResourceHandler("/admin/assets/**")
            .addResourceLocations("classpath:/static/admin/assets/")
            .setCachePeriod(86400) // Кеширование на 24 часа для assets
        
        registry.addResourceHandler("/admin/**")
            .addResourceLocations("classpath:/static/admin/")
            .setCachePeriod(3600) // Кеширование на 1 час для HTML
            .resourceChain(true)
        
        // Раздача статических файлов админки (assets, js, css) напрямую
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/admin/assets/")
            .setCachePeriod(86400) // Кеширование на 24 часа для assets
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Перенаправление корневого пути админки на index.html
        registry.addViewController("/admin")
            .setViewName("forward:/admin/index.html")
        registry.addViewController("/admin/")
            .setViewName("forward:/admin/index.html")
    }
}

