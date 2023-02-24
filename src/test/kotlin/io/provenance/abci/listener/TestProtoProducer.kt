package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.Properties
import org.apache.kafka.common.serialization.StringSerializer

/**
 * Produce Protobuf messages to Kafka.
 *
 * @param K The message key type.
 * @param V The specific Protobuf message value type.
 * @property schemaRegistryUrl Confluent Schema Registry URL
 * @constructor Creates a [TestProtoProducer] object.
 */
class TestProtoProducer<K, V : Message>(
    private val config: Config,
    private val schemaRegistryUrl: String? = null
) : TestProducer<K, V> {

    /**
     * Specifies the [bootstrapServers] for the producer properties.
     * @return the Kafka producer [Properties].
     */
    override fun createProducerProperties(bootstrapServers: String?): Properties {
        val props: Properties = config.getConfig("kafka.producer.kafka-clients").toProperties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ProtobufSerializer::class.java
        props[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG] = LoggingProducerInterceptor::class.qualifiedName

        // settings this property will tell the ProtobufSerializer ^^^ which serializer to use.
        if (!schemaRegistryUrl.isNullOrEmpty())
            props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl

        return props
    }

    /**
     * Specifies the [bootstrapServers] for the Kafka producer.
     * @return a Kafka [Producer] object.
     */
    override fun createProducer(bootstrapServers: String?): Producer<K, V> {
        val props = createProducerProperties(bootstrapServers!!)
        createTopics(createTopicsProps(bootstrapServers))
        return KafkaProducer(props)
    }

    private fun createTopicsProps(bootstrapServers: String?): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        props.putAll(config.getConfig("kafka.producer.listen-topics").toProperties())
        props["topic.partitions"] = config.getString("kafka.producer.topic.partitions")
        props["topic.replication.factor"] = config.getString("kafka.producer.topic.replication.factor")
        return props
    }
}
