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

        ListenTopic.values().forEach {
            topics.add(
                NewTopic(
                    envProps.getProperty(it.topic),
                    envProps.getProperty("topic.partitions").toInt(),
                    envProps.getProperty("topic.replication.factor").toShort()
                )
            )
        }
        client.createTopics(topics)
        client.close()
    }
}
