package com.naidizakupku.telegram.domain.admin

/**
 * Роли пользователей админки
 */
enum class AdminRole {
    /**
     * Администратор - полный доступ ко всем функциям
     */
    ADMIN,
    
    /**
     * Просмотрщик - только чтение данных, без возможности изменений
     */
    VIEWER
}

