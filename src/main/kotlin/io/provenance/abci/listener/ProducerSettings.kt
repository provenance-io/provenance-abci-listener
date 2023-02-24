package io.provenance.abci.listener

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serializer

class ProducerSettings<K, V>(
    private val config: Config,
    private val keySerializer: Serializer<K>? = null,
    private val valueSerializer: Serializer<V>? = null,
) {

    fun createKafkaProducer(): Producer<K, V> {
        val properties = config.getConfig("kafka-clients").toProperties()
        require(
            keySerializer != null ||
                    properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).isNotEmpty()) {
            "Key serializer should be defined or declared in configuration"
        }

        require(
            valueSerializer != null ||
                    properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).isNotEmpty()) {
            "Value serializer should be defined or declared in configuration"
        }

        return KafkaProducer(properties)
    }
}