package ru.spbu.apmath.pt

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import ru.spbu.apmath.pt.KafkaConfig.KafkaLocalConfig
import ru.spbu.apmath.pt.KafkaWordsProducer.SimpleProducer
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object KafkaWordsProducer {
    fun randInt(b: Int, e: Int): Int {
        return (b + Math.random() * (e - b)).toInt()
    }

    fun makeWord(): String {
        val words = listOf("hello", "world", "key", "news", "data", "ice")
        return words.random()
    }

    fun makeLine(): String {
        val sb = StringBuilder()
        val numWords = randInt(1, 10)
        for (i in 1..numWords) {
            sb.append(makeWord())
            if (i <= numWords - 1) {
                sb.append(' ')
            }
        }
        println(sb.toString())
        return sb.toString()
    }

    class SimpleProducer(private val props: Properties, private val topic: String) : Runnable {
        @Volatile
        private var isStopped = false

        fun stop() {
            isStopped = true
        }

        override fun run() {
            val producer = KafkaProducer(
                props, StringSerializer(), StringSerializer()
            )
            while (!isStopped) {
                val text = makeLine()
                val producerRecord = ProducerRecord(
                    topic, "text", text
                )
                producer.send(producerRecord)

                TimeUnit.SECONDS.sleep(randInt(1, 4).toLong())
            }
            producer.close()
        }
    }
}

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    Logger.getRootLogger().level = Level.INFO

    val props = KafkaLocalConfig()
        .consumerProps

    thread(start=true, block=SimpleProducer(props, KafkaConfig.TEXT_INPUT_TOPIC)::run)
}