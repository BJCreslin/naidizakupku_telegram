package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema

/**
 * DTO для статуса Kafka
 */
@Schema(description = "Статус Kafka")
data class KafkaStatusDto(
    @Schema(description = "Список топиков", required = true)
    val topics: List<TopicInfo>,
    
    @Schema(description = "Список consumer groups", required = true)
    val consumerGroups: List<ConsumerGroupInfo>,
    
    @Schema(description = "Доступен ли Kafka", example = "true", required = true)
    val isAvailable: Boolean
)

/**
 * Информация о топике
 */
@Schema(description = "Информация о топике Kafka")
data class TopicInfo(
    @Schema(description = "Название топика", example = "user-events", required = true)
    val name: String,
    
    @Schema(description = "Количество партиций", example = "3", required = true)
    val partitions: Int,
    
    @Schema(description = "Количество реплик", example = "1", required = true)
    val replicationFactor: Int,
    
    @Schema(description = "Размер топика (байт)", example = "1048576", required = true)
    val size: Long,
    
    @Schema(description = "Количество сообщений", example = "1000", required = true)
    val messageCount: Long
)

/**
 * Информация о consumer group
 */
@Schema(description = "Информация о consumer group")
data class ConsumerGroupInfo(
    @Schema(description = "Название consumer group", example = "naidizakupku-telegram-consumer", required = true)
    val groupId: String,
    
    @Schema(description = "Количество активных consumers", example = "2", required = true)
    val activeConsumers: Int,
    
    @Schema(description = "Количество топиков", example = "3", required = true)
    val topicsCount: Int,
    
    @Schema(description = "Статус группы", example = "STABLE", required = true)
    val state: String
)

