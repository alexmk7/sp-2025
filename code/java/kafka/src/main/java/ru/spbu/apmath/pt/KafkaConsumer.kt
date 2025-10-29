package ru.spbu.apmath.pt

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import ru.spbu.apmath.pt.KafkaConfig.KafkaLocalConfig
import ru.spbu.apmath.pt.KafkaWordsConsumer.SimpleConsumer
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

object KafkaWordsConsumer {

    class SimpleConsumer(private val props: Properties, private val topics: List<String>) : Runnable {
        @Volatile
        private var isStopped = false

        override fun run() {
            val consumer = KafkaConsumer(
                props, StringDeserializer(), StringDeserializer()
            )

            consumer.subscribe(topics)

            while (!isStopped) {
                val records = consumer.poll(Duration.ofSeconds(1))
                for (record in records) {
                    println("Topic: ${record.topic()}, message: (${record.key()}, ${record.value()}) at offset ${record.offset()}")
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    Logger.getRootLogger().level = Level.INFO

    val props = KafkaLocalConfig()
        .consumerProps

    val topics = listOf(KafkaConfig.VK_INPUT_TOPIC)
    thread(start = true, block = SimpleConsumer(props, topics)::run)
}