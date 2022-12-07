package io.provenance.abci.listener

import com.google.protobuf.Message
import java.lang.Exception
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

private val logger = KotlinLogging.logger {}

/**
 * Kafka [ProducerInterceptor] for logging on messages send by the [TestProtoProducer].
 */
class LoggingProducerInterceptor: ProducerInterceptor<String, Message> {
    override fun configure(configs: MutableMap<String, *>?) {}

    override fun close() {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {
        logger.info("Broker ACKed with metadata {}", metadata)
    }

    override fun onSend(record: ProducerRecord<String, Message>?): ProducerRecord<String, Message> {
        logger.info("Intercepted record: {}", record.toString())
        return record!!
    }
}