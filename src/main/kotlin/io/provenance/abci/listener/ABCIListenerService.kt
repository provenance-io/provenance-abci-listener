package io.provenance.abci.listener

import com.google.protobuf.Message
import mu.KotlinLogging
import network.cosmos.sdk.streaming.abci.v1.ABCIListenerServiceGrpcKt
import network.cosmos.sdk.streaming.abci.v1.Empty
import network.cosmos.sdk.streaming.abci.v1.ListenBeginBlockRequest
import network.cosmos.sdk.streaming.abci.v1.ListenCommitRequest
import network.cosmos.sdk.streaming.abci.v1.ListenDeliverTxRequest
import network.cosmos.sdk.streaming.abci.v1.ListenEndBlockRequest
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

private val logger = KotlinLogging.logger {}

/**
 * Implementation of the [ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase]
 */
class ABCIListenerService(
    private val topicPrefix: String,
    private val producer: Producer<String, Message>
) : ABCIListenerServiceGrpcKt.ABCIListenerServiceCoroutineImplBase() {

    override suspend fun listenBeginBlock(request: ListenBeginBlockRequest): Empty {
        val key = request.req.header.height.toString()
        return send(Topic.BEGIN_BLOCK.topic, key, request)
    }

    override suspend fun listenEndBlock(request: ListenEndBlockRequest): Empty {
        val key = request.req.height.toString()
        return send(Topic.END_BLOCK.topic, key, request)
    }

    override suspend fun listenDeliverTx(request: ListenDeliverTxRequest): Empty {
        val key = request.blockHeight.toString()
        return send(Topic.DELIVER_TX.topic, key, request)
    }

    override suspend fun listenCommit(request: ListenCommitRequest): Empty {
        val key = request.blockHeight.toString()
        return send(Topic.COMMIT.topic, key, request)
    }

    private suspend fun send(topicName: String, key: String, value: Message): Empty {
        val topic = "$topicPrefix$topicName"
        val record: ProducerRecord<String, Message> = ProducerRecord(topic, key, value)
        return producer.dispatch(record)
    }
}
