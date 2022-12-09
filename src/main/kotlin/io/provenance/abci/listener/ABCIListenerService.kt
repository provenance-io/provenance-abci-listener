package io.provenance.abci.listener

import com.google.protobuf.Message
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
 * Producer topic names for the [ABCIListenerService]
 */
enum class Topic(val topic: String) {
    BEGIN_BLOCK("listen-begin-block"),
    END_BLOCK("listen-end-block"),
    DELIVER_TX("listen-deliver-tx"),
    COMMIT("listen-commit")
}

/**
 * Implementation of the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase]
 */
class ABCIListenerService(
    private val topicPrefix: String,
    private val producer: Producer<String, Message>
) : ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase() {

    override suspend fun listenBeginBlock(request: ListenBeginBlockRequest): ListenBeginBlockResponse {
        val key = request.req.header.height.toString()
        send(Topic.BEGIN_BLOCK.topic, key, request)
            .also { return ListenBeginBlockResponse.newBuilder().build() }
    }

    override suspend fun listenEndBlock(request: ListenEndBlockRequest): ListenEndBlockResponse {
        val key = request.req.height.toString()
        send(Topic.END_BLOCK.topic, key, request)
            .also { return ListenEndBlockResponse.newBuilder().build() }
    }

    override suspend fun listenDeliverTx(request: ListenDeliverTxRequest): ListenDeliverTxResponse {
        val key = request.blockHeight.toString()
        send(Topic.DELIVER_TX.topic, key, request)
            .also { return ListenDeliverTxResponse.newBuilder().build() }
    }

    override suspend fun listenCommit(request: ListenCommitRequest): ListenCommitResponse {
        val key = request.blockHeight.toString()
        send(Topic.COMMIT.topic, key, request)
            .also { return ListenCommitResponse.newBuilder().build() }
    }

    private suspend fun send(topicName: String, key: String, value: Message): Any {
        val topic = "$topicPrefix$topicName"
        val record: ProducerRecord<String, Message> = ProducerRecord(topic, key, value)
        return producer.dispatch(record)
    }
}
