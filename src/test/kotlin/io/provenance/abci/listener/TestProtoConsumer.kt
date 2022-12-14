package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.Collections
import java.util.Properties
import java.util.Random

private val logger = KotlinLogging.logger {}

/**
 * Consume Protobuf messages from Kafka.
 *
 * @param K The message key type.
 * @param V The specific Protobuf message value type.
 * @property bootstrapServers  the Kafka broker address.
 * @property schemaRegistryUrl Confluent Schema Registry URL.
 * @property topic the topic name to consume messages from.
 * @property valueClass the specific Protobuf value type.
 * @constructor Creates a ProtoConsumer object.
 */
class TestProtoConsumer<K, V : Message>(
    private val bootstrapServers: String,
    private val schemaRegistryUrl: String,
    private val topic: String,
    private val valueClass: Class<V>
) : TestConsumer {

    private val config: Config = ConfigFactory.load()
    private val consumer: KafkaConsumer<K, V> = KafkaConsumer(createConsumerProperties(bootstrapServers))
    val messages: MutableList<ConsumerRecord<K, V>> = mutableListOf()

    init {
        val t: String = config.getString("kafka.consumer.input.topic.prefix") + topic
        consumer.subscribe(Collections.singleton(t))
    }

    /**
     * Specifies the [bootstrapServers] for the consumer properties.
     * @return the Kafka consumer [Properties].
     */
    override fun createConsumerProperties(bootstrapServers: String?): Properties {
        val props: Properties = config.getConfig("kafka.consumer.kafka-clients").toProperties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        props[ConsumerConfig.GROUP_ID_CONFIG] = "testgroup" + Random().nextInt()
        props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl

        // Specifying the value parameter `V` type is not enough. We need to specify a
        // specific protobuf value deserializer to be able to deserializer to a specific type.
        // This is because the Cosmos Protobuf files do not include: `java_outer_classname`
        // and `java_multiple_files = true` properties.
        // https://docs.confluent.io/platform/current/schema-registry/serdes-develop/serdes-protobuf.html#protobuf-deserializer
        props[KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE] = valueClass

        return props
    }

    override fun consume() {
        val records = consumer.poll(Duration.ofMillis(10000))
        for (record in records) {
            logger.info(
                "offset = {}, key = {}, value = {}",
                record.offset(),
                record.key(),
                record.value()
            )
            messages.add(record)
        }
    }

    fun close() {
        consumer.close(Duration.ofMillis(1000))
    }

    fun consumeAndClose() {
        consume()
        close()
    }
}
