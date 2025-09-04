package com.naidizakupku.telegram.config

import liquibase.integration.spring.SpringLiquibase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Конфигурация базы данных с условной загрузкой
 */
@Configuration
@ConditionalOnProperty(name = ["spring.datasource.url"])
class DatabaseConfig {
    
    /**
     * Конфигурация Liquibase с явным указанием XML файла
     */
    @Bean
    fun liquibase(dataSource: DataSource): SpringLiquibase {
        val liquibase = SpringLiquibase()
        liquibase.dataSource = dataSource
        liquibase.changeLog = "classpath:db/changelog/db.changelog-master.xml"
        liquibase.isDropFirst = false
        return liquibase
    }
}

