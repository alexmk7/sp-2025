package ru.spbu.apmath.pt

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import java.nio.file.Paths
import java.util.*

object KafkaConfig {
    val VK_INPUT_TOPIC = "vk-input"

    val BOOTSTRAP_SERVERS = listOf("localhost:9092")

    class KafkaLocalConfig(
        private val servers: List<String> = BOOTSTRAP_SERVERS,
    ) {
        val consumerProps: Properties
            get() {
                val props = Properties()
                props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
                props[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] = "1000"
                props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "30000"
                props[ConsumerConfig.GROUP_ID_CONFIG] = "1"
                props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] =
                    "org.apache.kafka.common.serialization.StringDeserializer"
                props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] =
                    "org.apache.kafka.common.serialization.StringDeserializer"
                props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers.joinToString(",")
                props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] =
                    "org.apache.kafka.common.serialization.StringSerializer"
                props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =
                    "org.apache.kafka.common.serialization.StringSerializer"

                return props
            }
    }
}