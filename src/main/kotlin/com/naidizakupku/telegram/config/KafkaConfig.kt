package com.naidizakupku.telegram.config

import com.naidizakupku.telegram.domain.dto.AuthorizationRevokeResponseDto
import com.naidizakupku.telegram.domain.dto.CodeVerificationRequestDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {
    
    @Value("\${kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String
    
    @Value("\${kafka.consumer.group-id:telegram-bot-verification}")
    private lateinit var groupId: String
    
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true
        )
        return DefaultKafkaProducerFactory(configProps)
    }
    
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
    
    @Bean
    fun consumerFactory(): ConsumerFactory<String, CodeVerificationRequestDto> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false
        )
        return DefaultKafkaConsumerFactory(
            configProps,
            StringDeserializer(),
            JsonDeserializer(CodeVerificationRequestDto::class.java)
        )
    }
    
    @Bean
    fun revokeResponseConsumerFactory(): ConsumerFactory<String, AuthorizationRevokeResponseDto> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "${groupId}-revoke",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false
        )
        return DefaultKafkaConsumerFactory(
            configProps,
            StringDeserializer(),
            JsonDeserializer(AuthorizationRevokeResponseDto::class.java)
        )
    }
    
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, CodeVerificationRequestDto> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, CodeVerificationRequestDto>()
        factory.consumerFactory = consumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
//        factory.setMessageConverter(StringJsonMessageConverter())
        return factory
    }
    
    @Bean
    fun revokeResponseListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, AuthorizationRevokeResponseDto> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AuthorizationRevokeResponseDto>()
        factory.consumerFactory = revokeResponseConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
//        factory.setMessageConverter(StringJsonMessageConverter())
        return factory
    }
}

