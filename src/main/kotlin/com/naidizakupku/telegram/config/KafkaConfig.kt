package com.naidizakupku.telegram.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

/**
 * Конфигурация Kafka
 */
@Configuration
class KafkaConfig {
    
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String
    
    @Value("\${spring.kafka.security.username:}")
    private lateinit var username: String
    
    @Value("\${spring.kafka.security.password:}")
    private lateinit var password: String
    
    /**
     * Конфигурация Producer Factory
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        
        // Настройки безопасности если указаны
        if (username.isNotEmpty() && password.isNotEmpty()) {
            configProps["security.protocol"] = "SASL_SSL"
            configProps["sasl.mechanism"] = "PLAIN"
            configProps["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        }
        
        return DefaultKafkaProducerFactory(configProps)
    }
    
    /**
     * Kafka Template
     */
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
    
    /**
     * Топик для пользовательских событий
     */
    @Bean
    fun userEventsTopic(): NewTopic {
        return TopicBuilder.name("user-events")
            .partitions(3)
            .replicas(1)
            .build()
    }
    
    /**
     * Топик для уведомлений
     */
    @Bean
    fun notificationsTopic(): NewTopic {
        return TopicBuilder.name("notifications")
            .partitions(3)
            .replicas(1)
            .build()
    }
}

