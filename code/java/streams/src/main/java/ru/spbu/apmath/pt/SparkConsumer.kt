package ru.spbu.apmath.pt

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies
import ru.spbu.apmath.pt.KafkaConfig.KafkaLocalConfig

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    Logger.getRootLogger().level = Level.FATAL

    val conf = SparkConf().setAppName("Spark streaming example")
        .setMaster("local[*]")

    val ssc = JavaStreamingContext(conf, Durations.seconds(1))
    val props = KafkaLocalConfig()
        .consumerProps

    val propsMap = props.mapKeys { kv -> kv.key.toString() }

    val topics = listOf(KafkaConfig.TEXT_INPUT_TOPIC)
    val stream = KafkaUtils.createDirectStream(
        ssc,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe<String, String>(topics, propsMap)
    )
    stream.map { t: ConsumerRecord<String, String> -> t.value() }
        .window(Durations.seconds(10), Durations.seconds(2))
        .flatMap { x: String -> x.split(Regex("\\s+")).iterator() }
        .countByValue()
        .foreachRDD { rdd ->
            println(
                rdd.map { t -> t.swap() }
                    .sortBy({ t -> -t._1!! }, false, 1)
                    .take(5)
                    .toString()
            )
        }
    ssc.start()
    ssc.awaitTermination()
}