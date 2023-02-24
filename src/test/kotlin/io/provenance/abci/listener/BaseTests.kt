package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import cosmos.streaming.abci.v1.ABCIListenerServiceGrpcKt
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.testing.GrpcCleanupRule
import net.christophschubert.cp.testcontainers.CPTestContainerFactory
import net.christophschubert.cp.testcontainers.SchemaRegistryContainer
import org.apache.kafka.clients.producer.Producer
import org.junit.Rule
import org.testcontainers.containers.KafkaContainer

open class BaseTests {

    protected val testContainerFactory: CPTestContainerFactory = CPTestContainerFactory()
    protected val kafka: KafkaContainer = testContainerFactory.createKafka()
    protected lateinit var schemaRegistry: SchemaRegistryContainer

    protected val config: Config = ConfigFactory.load("test.conf")
    protected val topicConfig: Config = config.getConfig("kafka.producer.listen-topics")

    protected lateinit var producer: Producer<String, Message>

    @get:Rule
    val grpcCleanupRule: GrpcCleanupRule = GrpcCleanupRule()
}
