package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

/**
 * Produce Protobuf messages to Kafka.
 *
 * @param K The message key type.
 * @param V The specific Protobuf message value type.
 * @property schemaRegistryUrl Confluent Schema Registry URL
 * @constructor Creates a [TestProtoProducer] object.
 */
class TestProtoProducer<K, V : Message>(private val schemaRegistryUrl: String) : TestProducer<K, V> {
    private val config = ConfigFactory.load()

    /**
     * Specifies the [bootstrapServers] for the producer properties.
     * @return the Kafka producer [Properties].
     */
    override fun createProducerProperties(bootstrapServers: String?): Properties {
        val props: Properties = config.getConfig("kafka.producer.kafka-clients").toProperties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
        props[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG] = LoggingProducerInterceptor::class.qualifiedName

        props["input.topic.prefix"] = config.getString("kafka.producer.input.topic.prefix")
        props["input.topic.partitions"] = config.getString("kafka.producer.input.topic.partitions")
        props["input.topic.replication.factor"] = config.getString("kafka.producer.input.topic.replication.factor")

        return props
    }

    /**
     * Specifies the [bootstrapServers] for the Kafka producer.
     * @return a Kafka [Producer] object.
     */
    override fun createProducer(bootstrapServers: String?): Producer<K, V> {
        val props = createProducerProperties(bootstrapServers!!)
        createTopics(props)
        return KafkaProducer(props)
    }
}
