package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Общий класс для пагинированных ответов
 */
@Schema(description = "Пагинированный ответ")
data class PagedResponse<T>(
    @Schema(description = "Список элементов", required = true)
    val content: List<T>,
    
    @Schema(description = "Номер текущей страницы (начиная с 0)", example = "0", required = true)
    val page: Int,
    
    @Schema(description = "Размер страницы", example = "20", required = true)
    val size: Int,
    
    @Schema(description = "Общее количество элементов", example = "100", required = true)
    val totalElements: Long,
    
    @Schema(description = "Общее количество страниц", example = "5", required = true)
    val totalPages: Int,
    
    @Schema(description = "Есть ли следующая страница", example = "true", required = true)
    val hasNext: Boolean,
    
    @Schema(description = "Есть ли предыдущая страница", example = "false", required = true)
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PagedResponse<T> {
            val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
            return PagedResponse(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = page < totalPages - 1,
                hasPrevious = page > 0
            )
        }
    }
}

