package ru.spbu.apmath.pt


import ru.spbu.apmath.pt.KafkaConfig.KafkaLocalConfig
import java.lang.Thread
import ru.spbu.apmath.pt.KafkaWordsProducer.SimpleProducer
import ru.spbu.apmath.pt.KafkaWordsConsumer.SimpleConsumer
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.KafkaStreams
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.util.*
import kotlin.concurrent.thread

object KafkaStreamsWordCount {

    fun streamConfig(): Properties {
        val conf = Properties()
        conf[StreamsConfig.APPLICATION_ID_CONFIG] = "wordcount-lambda-example"
        conf[StreamsConfig.CLIENT_ID_CONFIG] = "wordcount-lambda-example-client"
        conf[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = KafkaConfig.BOOTSTRAP_SERVERS
        conf[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
        conf[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
        conf[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 10 * 1000
        conf[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0

        return conf
    }
}

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    Logger.getRootLogger().level = Level.INFO

    val pattern = Regex("\\W+")

    val streamsConfiguration = KafkaStreamsWordCount.streamConfig()

    val stringSerde = Serdes.String()
    val builder = StreamsBuilder()
    val textLines = builder.stream<String, String>(KafkaConfig.TEXT_INPUT_TOPIC)

    val wordCounts = textLines
        .flatMapValues { value: String -> pattern.split(value.lowercase()) }
        .groupBy { key: String?, word: String? -> word }
        .count()

    wordCounts
        .toStream()
        .mapValues { value: Long -> value.toString() }
        .to(KafkaConfig.WORD_COUNT_OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde))

    wordCounts.toStream().foreach { k, v -> print("$k $v\n") }

    val streams = KafkaStreams(builder.build(), streamsConfiguration)
    streams.cleanUp()
    streams.start()

    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
}