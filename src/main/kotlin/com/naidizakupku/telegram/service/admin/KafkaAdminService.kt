package com.naidizakupku.telegram.service.admin

import com.naidizakupku.telegram.domain.dto.admin.KafkaStatusDto
import com.naidizakupku.telegram.domain.dto.admin.ConsumerGroupInfo
import com.naidizakupku.telegram.domain.dto.admin.TopicInfo
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.DescribeTopicsResult
import org.apache.kafka.clients.admin.ListConsumerGroupsResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Сервис для работы с Kafka в админке
 */
@Service
class KafkaAdminService(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}") private val bootstrapServers: String
) {

    private val logger = LoggerFactory.getLogger(KafkaAdminService::class.java)

    /**
     * Получить статус Kafka
     */
    fun getKafkaStatus(): KafkaStatusDto {
        var adminClient: AdminClient? = null
        return try {
            val props = Properties()
            props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
            adminClient = AdminClient.create(props)
            
            val topics = getTopics(adminClient)
            val consumerGroups = getConsumerGroups(adminClient)
            
            KafkaStatusDto(
                topics = topics,
                consumerGroups = consumerGroups,
                isAvailable = true
            )
        } catch (e: Exception) {
            logger.error("Error getting Kafka status: ${e.message}", e)
            KafkaStatusDto(
                topics = emptyList(),
                consumerGroups = emptyList(),
                isAvailable = false
            )
        } finally {
            adminClient?.close()
        }
    }

    /**
     * Получить список топиков
     */
    private fun getTopics(adminClient: AdminClient): List<TopicInfo> {
        return try {
            val topicsResult = adminClient.listTopics()
            val topicNames = topicsResult.names().get(5, TimeUnit.SECONDS)
            
            if (topicNames.isEmpty()) {
                return emptyList()
            }

            val describeResult: DescribeTopicsResult = adminClient.describeTopics(topicNames)
            val topicDescriptions = describeResult.allTopicNames().get(5, TimeUnit.SECONDS)

            topicDescriptions.map { (name, description) ->
                TopicInfo(
                    name = name,
                    partitions = description.partitions().size,
                    replicationFactor = description.partitions().firstOrNull()?.replicas()?.size ?: 0,
                    size = 0L, // Размер топика сложно получить без дополнительных вызовов
                    messageCount = 0L // Количество сообщений требует дополнительных вызовов
                )
            }
        } catch (e: Exception) {
            logger.error("Error getting topics: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Получить список consumer groups
     */
    private fun getConsumerGroups(adminClient: AdminClient): List<ConsumerGroupInfo> {
        return try {
            val groupsResult: ListConsumerGroupsResult = adminClient.listConsumerGroups()
            val groups = groupsResult.all().get(5, TimeUnit.SECONDS)

            groups.map { group ->
                ConsumerGroupInfo(
                    groupId = group.groupId(),
                    activeConsumers = 0, // Требует дополнительных вызовов
                    topicsCount = 0, // Требует дополнительных вызовов
                    state = group.state().toString()
                )
            }
        } catch (e: Exception) {
            logger.error("Error getting consumer groups: ${e.message}", e)
            emptyList()
        }
    }
}

