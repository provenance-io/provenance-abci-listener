package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.grpc.Server
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.HealthStatusManager
import io.grpc.protobuf.services.ProtoReflectionService
import org.apache.kafka.clients.producer.Producer
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * The [ABCIListenerServer] starts the gRPC server for the [AbciListenerService].
 * In addition to the [AbciListenerService], a health service (managed by [HealthStatusManager])
 * and a [ProtoReflectionService] are also added.
 *
 * The server is also responsible for initializing and closing the Kafka [Producer]
 * used by the [AbciListenerService].
 */
class AbciListenerServer {
    private val config: Config = ConfigFactory.load()
    private val inet = InetSocketAddress(
        config.getString("grpc.server.addr"),
        config.getInt("grpc.server.port")
    )

    // Kafka producer
    private var producerConfig: Config = config.getConfig("kafka.producer")
    private var topicConfig: Config = producerConfig.getConfig("listen-topics")
    private val producer = ProducerSettings<String, Message>(producerConfig).createKafkaProducer()

    private val health: HealthStatusManager = HealthStatusManager()
    private val server: Server = NettyServerBuilder
        .forAddress(inet)
        .directExecutor()
        .addService(health.healthService)
        .addService(ProtoReflectionService.newInstance())
        .addService(AbciListenerService(topicConfig, producer))
        .build()

    /** Start the [AbciListenerService] gRPC server. */
    fun start() {
        server.start()

        // set the plugin health status
        health.setStatus("plugin", ServingStatus.SERVING)

        // Output handshake information
        // https://github.com/hashicorp/go-plugin/blob/master/docs/guide-plugin-write-non-go.md#4-output-handshake-information
        println("1|1|tcp|${inet.address.hostAddress}:${inet.port}|grpc")

        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@AbciListenerServer.stop()
                println("*** server shut down")
            }
        )
    }

    /** Stop the [AbciListenerService] gRPC server. */
    private fun stop() {
        // shutdown producer
        producer.close(Duration.ofMillis(3000))

        // Start graceful shutdown
        server.shutdown()
        try {
            // Wait for RPCs to complete processing
            if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                // That was plenty of time. Let's cancel the remaining RPCs
                server.shutdownNow()
                // shutdownNow isn't instantaneous, so give a bit of time to clean resources up
                // gracefully. Normally this will be well under a second.
                server.awaitTermination(5, TimeUnit.SECONDS)
            }
        } catch (ex: InterruptedException) {
            server.shutdownNow()
        }
    }

    /**
     * [description copied from [io.grpc.Server.awaitTermination]]
     *
     * Waits for the server to become terminated.
     * Calling this method before start() or shutdown()
     * is permitted and does not change its behavior.
     * See [io.grpc.Server.awaitTermination] for more details.
     */
    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    val server = AbciListenerServer()
    server.start()
    server.blockUntilShutdown()
}
