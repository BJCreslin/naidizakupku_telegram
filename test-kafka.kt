import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

fun main() {
    // Конфигурация Producer
    val props = Properties()
    props["bootstrap.servers"] = "localhost:9092"
    props["key.serializer"] = StringSerializer::class.java.name
    props["value.serializer"] = StringSerializer::class.java.name
    props["acks"] = "all"
    props["retries"] = "3"
    
    val producer = KafkaProducer<String, String>(props)
    
    try {
        // Тестовое сообщение
        val testMessage = """
            {
                "userId": 123456,
                "eventType": "test_event",
                "timestamp": ${System.currentTimeMillis()},
                "data": {
                    "message": "Тестовое сообщение из скрипта",
                    "source": "test-kafka.kt"
                }
            }
        """.trimIndent()
        
        val record = ProducerRecord("user-events", "test-key", testMessage)
        val result = producer.send(record).get()
        
        println("✅ Сообщение успешно отправлено!")
        println("📝 Топик: ${result.recordMetadata.topic()}")
        println("📊 Partition: ${result.recordMetadata.partition()}")
        println("📍 Offset: ${result.recordMetadata.offset()}")
        println("📨 Сообщение: $testMessage")
        
    } catch (e: Exception) {
        println("❌ Ошибка отправки сообщения: ${e.message}")
        e.printStackTrace()
    } finally {
        producer.close()
    }
}
