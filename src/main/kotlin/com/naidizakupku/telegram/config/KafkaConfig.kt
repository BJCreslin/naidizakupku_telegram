package com.naidizakupku.telegram.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.slf4j.LoggerFactory

/**
 * Конфигурация Kafka
 */
@Configuration
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"], matchIfMissing = false)
class KafkaConfig {
    
    private val logger = LoggerFactory.getLogger(KafkaConfig::class.java)
    
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String
    
    @Value("\${spring.kafka.security.username:}")
    private lateinit var username: String
    
    @Value("\${spring.kafka.security.password:}")
    private lateinit var password: String
    
    @Value("\${spring.kafka.consumer.group-id:naidizakupku-telegram-consumer}")
    private lateinit var groupId: String
    
    init {
        logger.info("Инициализация Kafka конфигурации для серверов: $bootstrapServers")
        logger.info("Username: '${if (username.isBlank()) "не указан" else username}'")
        logger.info("Password: '${if (password.isBlank()) "не указан" else "***"}'")
    }
    
    /**
     * Конфигурация Producer Factory
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        
        // Настройки безопасности только если указаны username и password
        if (username.isNotBlank() && password.isNotBlank()) {
            configProps["security.protocol"] = "SASL_SSL"
            configProps["sasl.mechanism"] = "PLAIN"
            configProps["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        } else {
            // Для PLAINTEXT подключения без аутентификации
            configProps["security.protocol"] = "PLAINTEXT"
        }
        
        return DefaultKafkaProducerFactory(configProps)
    }
    
    /**
     * Конфигурация Consumer Factory
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "500"
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "30000"
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = "3000"
        
        // Настройки безопасности только если указаны username и password
        if (username.isNotBlank() && password.isNotBlank()) {
            configProps["security.protocol"] = "SASL_SSL"
            configProps["sasl.mechanism"] = "PLAIN"
            configProps["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        } else {
            // Для PLAINTEXT подключения без аутентификации
            configProps["security.protocol"] = "PLAINTEXT"
        }
        
        return DefaultKafkaConsumerFactory(configProps)
    }
    
    /**
     * Kafka Template
     */
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
    
    /**
     * Kafka Listener Container Factory
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.setConcurrency(3)
        return factory
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

