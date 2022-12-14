package io.provenance.abci.listener

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.Producer
import java.util.Properties

interface TestConsumer {
    fun createConsumerProperties(bootstrapServers: String?): Properties
    fun consume()
}

interface TestProducer<K, V> {
    fun createProducerProperties(bootstrapServers: String?): Properties
    fun createProducer(bootstrapServers: String?): Producer<K, V>

    fun createTopics(envProps: Properties) {
        val config = mapOf("bootstrap.servers" to envProps.getProperty("bootstrap.servers"))
        val client = AdminClient.create(config)
        val topics = mutableListOf<NewTopic>()

        Topic.values().forEach {
            topics.add(
                NewTopic(
                    envProps.getProperty("input.topic.prefix") + it.topic,
                    envProps.getProperty("input.topic.partitions").toInt(),
                    envProps.getProperty("input.topic.replication.factor").toShort()
                )
            )
        }
        client.createTopics(topics)
        client.close()
    }
}
