package ru.spbu.apmath.pt

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.streaming.clients.StreamingEventHandler
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient
import com.vk.api.sdk.streaming.clients.actors.StreamingActor
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import com.vk.api.sdk.streaming.objects.StreamingRule
import com.vk.api.sdk.streaming.objects.responses.StreamingGetRulesResponse
import com.vk.api.sdk.streaming.objects.responses.StreamingResponse
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import ru.spbu.apmath.pt.KafkaConfig.KafkaLocalConfig
import java.util.*

object VkProducer {

    val transportClient = HttpTransportClient()
    val vkClient = VkApiClient(transportClient)
    val streamingClient = VkStreamingApiClient(transportClient)

    val vkCredentials: Properties
        get() {
            VkProducer::class.java.getResource("/vk_credentials.conf")?.openStream().use {
                val props = Properties()
                props.load(it)
                return props
            }
        }

    val servActor = ServiceActor(
        vkCredentials.getProperty("app_id").toInt(),
        vkCredentials.getProperty("access_token")
    )

    val serverUrlResponse = vkClient.streaming().getServerUrl(servActor).execute()
    val actor = StreamingActor(serverUrlResponse.endpoint, serverUrlResponse.key)

    fun listenKafka(props: Properties, topic: String) {
        val producer = KafkaProducer<String, String>(props)

        streamingClient.stream().get(actor, object : StreamingEventHandler() {
            override fun handle(msg: StreamingCallbackMessage) {
                println(msg)

                val data = ProducerRecord(topic, "VK", msg.event.text)
                producer.send(data)
            }
        }).execute()
    }

    fun addRule(tag: String, value: String) {
        streamingClient.rules().add(actor, tag, value).execute<StreamingResponse>()
    }

    fun getRules(): List<StreamingRule> {
        val resp = streamingClient.rules().get(actor).execute<StreamingGetRulesResponse>()
        return if (resp.rules != null) resp.rules else emptyList()
    }
}

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    Logger.getRootLogger().setLevel(Level.ERROR)

    if (VkProducer.getRules().isEmpty()) {
        VkProducer.addRule("0", "привет питер кот мэр собака")
    }
    VkProducer.listenKafka(KafkaLocalConfig().consumerProps, KafkaConfig.VK_INPUT_TOPIC)
}