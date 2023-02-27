package io.provenance.abci.listener

import com.google.protobuf.Message
import com.typesafe.config.Config
import cosmos.streaming.abci.v1.ABCIListenerServiceGrpcKt
import cosmos.streaming.abci.v1.Grpc.ListenBeginBlockRequest
import cosmos.streaming.abci.v1.Grpc.ListenBeginBlockResponse
import cosmos.streaming.abci.v1.Grpc.ListenCommitRequest
import cosmos.streaming.abci.v1.Grpc.ListenCommitResponse
import cosmos.streaming.abci.v1.Grpc.ListenDeliverTxRequest
import cosmos.streaming.abci.v1.Grpc.ListenDeliverTxResponse
import cosmos.streaming.abci.v1.Grpc.ListenEndBlockRequest
import cosmos.streaming.abci.v1.Grpc.ListenEndBlockResponse
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * Producer topic names for the [AbciListenerService]
 */
enum class ListenTopic(val topic: String) {
    BEGIN_BLOCK("listen-begin-block"),
    END_BLOCK("listen-end-block"),
    DELIVER_TX("listen-deliver-tx"),
    COMMIT("listen-commit")
}

/**
 * Implementation of the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase]
 *
 * @property topicConfig the `kafka.producer.listen-topics` [Config] object from the `application.conf`.
 * @property producer the Kafka Protobuf [Producer].
 * @constructor Creates a gRPC ABCI listener service.
 */
class AbciListenerService(
    private val topicConfig: Config,
    private val producer: Producer<String, Message>
) : ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase() {

    /**
     * Process begin block [request]s for the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineStub.listenBeginBlock] RPC method.
     */
    override suspend fun listenBeginBlock(request: ListenBeginBlockRequest): ListenBeginBlockResponse {
        val key = request.req.header.height.toString()
        send(ListenTopic.BEGIN_BLOCK, key, request)
            .also { return ListenBeginBlockResponse.newBuilder().build() }
    }

    /**
     * Process begin block [request]s for the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineStub.listenEndBlock] RPC method.
     */
    override suspend fun listenEndBlock(request: ListenEndBlockRequest): ListenEndBlockResponse {
        val key = request.req.height.toString()
        send(ListenTopic.END_BLOCK, key, request)
            .also { return ListenEndBlockResponse.newBuilder().build() }
    }

    /**
     * Process begin block [request]s for the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineStub.listenDeliverTx] RPC method.
     */
    override suspend fun listenDeliverTx(request: ListenDeliverTxRequest): ListenDeliverTxResponse {
        val key = request.blockHeight.toString()
        send(ListenTopic.DELIVER_TX, key, request)
            .also { return ListenDeliverTxResponse.newBuilder().build() }
    }

    /**
     * Process begin block [request]s for the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineStub.listenCommit] RPC method.
     */
    override suspend fun listenCommit(request: ListenCommitRequest): ListenCommitResponse {
        val key = request.blockHeight.toString()
        send(ListenTopic.COMMIT, key, request)
            .also { return ListenCommitResponse.newBuilder().build() }
    }

    /**
     * Sends a [key] and [value] record to the [listenTopic].
     */
    private suspend fun send(listenTopic: ListenTopic, key: String, value: Message): Any {
        val topic = topicConfig.getString(listenTopic.topic)
        val record: ProducerRecord<String, Message> = ProducerRecord(topic, key, value)
        return producer.dispatch(record)
    }
}
