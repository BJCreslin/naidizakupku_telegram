package com.naidizakupku.telegram.config

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import org.springframework.kafka.support.serializer.JsonDeserializer as SpringJsonDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*


@Configuration
@EnableKafka
class KafkaConfig {
    
    @Value("\${kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String
    
    @Value("\${spring.kafka.consumer.group-id:naidizakupku-telegram-consumer}")
    private lateinit var groupId: String
    
    @Value("\${kafka.verification.consumer.group-id:telegram-bot-verification}")
    private lateinit var verificationGroupId: String

    // Producer factories
    @Bean
    fun stringProducerFactory(): ProducerFactory<String?, String?> {
        val configProps: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        return DefaultKafkaProducerFactory<String?, String?>(configProps)
    }

    @Bean
    fun objectProducerFactory(): ProducerFactory<String?, Any?> {
        val configProps: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
        configProps.put(ProducerConfig.ACKS_CONFIG, "all")
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3)
        return DefaultKafkaProducerFactory<String?, Any?>(configProps)
    }

    // Kafka templates
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String?, String?> {
        return KafkaTemplate<String?, String?>(stringProducerFactory())
    }

    @Bean
    fun kafkaObjectTemplate(): KafkaTemplate<String?, Any?> {
        return KafkaTemplate<String?, Any?>(objectProducerFactory())
    }

    // Consumer factories
    @Bean
    fun stringConsumerFactory(): org.springframework.kafka.core.ConsumerFactory<String?, String?> {
        val props: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        return DefaultKafkaConsumerFactory<String?, String?>(props)
    }

    @Bean
    fun objectConsumerFactory(): ConsumerFactory<String?, Any?> {
        val props: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpringJsonDeserializer::class.java)
        props.put("spring.json.trusted.packages", "*")
        props.put("spring.json.use.type.headers", false)
        props.put("spring.json.value.default.type", "java.lang.String")
        return DefaultKafkaConsumerFactory<String?, Any?>(props)
    }

    // Listener container factories
    @Bean
    fun stringKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String?, String?> {
        val factory = ConcurrentKafkaListenerContainerFactory<String?, String?>()
        factory.setConsumerFactory(stringConsumerFactory())
        return factory
    }


    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String?, Any?> {
        val factory = ConcurrentKafkaListenerContainerFactory<String?, Any?>()
        factory.setConsumerFactory(objectConsumerFactory())
        return factory
    }

    // Consumer factory для верификации
    @Bean
    fun verificationConsumerFactory(): ConsumerFactory<String?, Any?> {
        val props: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, verificationGroupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpringJsonDeserializer::class.java)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
        props.put("spring.json.trusted.packages", "*")
        props.put("spring.json.use.type.headers", false)
        props.put("spring.json.value.default.type", "java.lang.String")
        return DefaultKafkaConsumerFactory<String?, Any?>(props)
    }

    @Bean
    fun verificationKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String?, Any?> {
        val factory = ConcurrentKafkaListenerContainerFactory<String?, Any?>()
        factory.setConsumerFactory(verificationConsumerFactory())
        return factory
    }
}

