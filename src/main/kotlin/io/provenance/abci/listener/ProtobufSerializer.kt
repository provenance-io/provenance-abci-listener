package io.provenance.abci.listener

import com.google.protobuf.Message
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
import java.io.ByteArrayOutputStream
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.Serializer

/**
 * ProtobufSerializer is a custom Protobuf serializer for Kafka
 * that chooses a serialization path based on whether the application
 * is configured to use the Confluent Schema Registry or not.
 *
 * When the application is configured to use the Confluent Schema Registry
 * the [KafkaProtobufSerializer] will be used. Otherwise, the [ByteArraySerializer] will be used.
 *
 * In order to enable the application to use the Confluent Schema Registry,
 * you MUST set the following properties:
 *
 *  `schema.registry.url={{ SR_URL }}`
 *  `value.serializer=io.provenance.abci.listener.ProtobufSerializer`.
 *
 *  Example configuration:
 *
 *      ```
 *      # application.conf
 *
 *      # application configuration properties
 *      ...
 *
 *      kafka.producer {
 *          ...
 *          # Properties defined by org.apache.kafka.clients.producer.ProducerConfig.
 *          # can be defined in this configuration section.
 *          kafka-clients {
 *              bootstrap.servers = "localhost:9092"
 *              // other producer properties
 *              ...
 *              key.serializer = org.apache.kafka.common.serialization.StringSerializer
 *              value.serializer = io.provenance.abci.listener.ProtobufSerializer
 *              schema.registry.url = "http://127.0.0.1:8081"
 *          }
 *      }
 *      ```
 * For additional producer properties see [org.apache.kafka.clients.producer.ProducerConfig]
 *
 */
class ProtobufSerializer<T : Message> : Serializer<T> {
    private lateinit var serializer: Serializer<T>

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        val useSchemaRegistry = configs!!.containsKey(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)
        if (useSchemaRegistry) {
            val serializer = KafkaProtobufSerializer<T>()
            serializer.configure(configs, isKey)
            this.serializer = serializer
        } else {
            val serializer = ByteArraySerializer()
            @Suppress("UNCHECKED_CAST")
            this.serializer = serializer as Serializer<T>
        }
    }

    override fun serialize(topic: String?, data: T): ByteArray {
        var bytes: ByteArray = byteArrayOf()
        when (serializer) {
            is KafkaProtobufSerializer -> bytes = serializer.serialize(topic, data)
            is ByteArraySerializer -> {
                val out = ByteArrayOutputStream()
                data.writeTo(out)
                bytes = out.toByteArray()
                out.close()
            }
        }
        return bytes
    }
}
