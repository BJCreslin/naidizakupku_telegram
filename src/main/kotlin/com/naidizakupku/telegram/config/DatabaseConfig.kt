package com.naidizakupku.telegram.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Конфигурация базы данных
 */
@Configuration
@EnableJpaRepositories(basePackages = ["com.naidizakupku.telegram.repository"])
@EnableTransactionManagement
class DatabaseConfig

