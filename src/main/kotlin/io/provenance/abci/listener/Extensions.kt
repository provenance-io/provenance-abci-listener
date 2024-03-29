package io.provenance.abci.listener

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import io.grpc.Status
import io.grpc.StatusException
import mu.KotlinLogging
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Convert a [Config] object to [java.util.Properties].
 *
 * This method supports only basic mapping where the `value` is a primitive type.
 * Throws [ConfigException.WrongType] otherwise.
 */
fun Config.toProperties(): Properties {
    val props = Properties()
    this.entrySet().forEach { props.setProperty(it.key, this.getString(it.key)) }
    return props
}

/**
 * Add a [dispatch] method to the Kafka [Producer] to produce messages
 * in a non-blocking manner and `await` for acknowledgement from broker
 * before responding on gRPC endpoints in [AbciListenerService].
 *
 * Resumes with a [StatusException] when an exception is encountered.
 */
suspend inline fun <reified K : Any, reified V : Any> Producer<K, V>.dispatch(record: ProducerRecord<K, V>): Any =
    suspendCoroutine { continuation ->
        val logger = KotlinLogging.logger {}
        val callback = Callback { metadata, exception ->
            if (exception != null) {
                logger.error("failed to produce message", exception)
                continuation.resumeWithException(
                    StatusException(
                        Status.INTERNAL.withDescription(exception.stackTraceToString())
                    )
                )
            } else {
                logger.debug("{}", metadata)
                continuation.resume(Unit)
            }
        }
        this.send(record, callback)
    }
