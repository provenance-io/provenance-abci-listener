package io.provenance.abci.listener

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import cosmos.base.store.v1beta1.Listening.StoreKVPair
import cosmos.streaming.abci.v1.Grpc.ListenBeginBlockRequest
import cosmos.streaming.abci.v1.Grpc.ListenBeginBlockResponse
import cosmos.streaming.abci.v1.Grpc.ListenCommitRequest
import cosmos.streaming.abci.v1.Grpc.ListenCommitResponse
import cosmos.streaming.abci.v1.Grpc.ListenDeliverTxRequest
import cosmos.streaming.abci.v1.Grpc.ListenDeliverTxResponse
import cosmos.streaming.abci.v1.Grpc.ListenEndBlockRequest
import cosmos.streaming.abci.v1.Grpc.ListenEndBlockResponse
import io.grpc.inprocess.InProcessServerBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tendermint.abci.Types
import tendermint.abci.Types.RequestBeginBlock
import tendermint.abci.Types.RequestDeliverTx
import tendermint.abci.Types.RequestEndBlock
import tendermint.abci.Types.ResponseBeginBlock
import tendermint.abci.Types.ResponseCommit
import tendermint.abci.Types.ResponseDeliverTx
import tendermint.abci.Types.ResponseEndBlock
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbciListenerServerWithSchemaRegistryTests : BaseTests() {

    @BeforeAll
    internal fun setUpAll() {
        schemaRegistry = testContainerFactory.createSchemaRegistry(kafka)
        schemaRegistry.start()
        producer = TestProtoProducer<String, Message>(config, schemaRegistry.baseUrl)
            .createProducer(kafka.bootstrapServers)
    }

    @AfterAll
    internal fun tearDownAll() {
        producer.close()
        schemaRegistry.stop()
        kafka.stop()
    }

    @Test
    fun listenBeginBlock(): Unit = runBlocking {
        val time = Instant.now()

        grpcCleanupRule.register(
            InProcessServerBuilder.forName("listenBeginBlock").directExecutor()
                .addService(AbciListenerService(topicConfig, producer))
                .build()
                .start()
        )

        val reply: ListenBeginBlockResponse = listenBeginBlockStub.listenBeginBlock(
            ListenBeginBlockRequest.newBuilder()
                .setReq(
                    RequestBeginBlock.newBuilder()
                        .setHeader(
                            tendermint.types.Types.Header.newBuilder()
                                .setHeight(1)
                                .setTime(
                                    Timestamp.newBuilder()
                                        .setSeconds(time.epochSecond)
                                        .setNanos(time.nano)
                                        .build()
                                )
                                .build()
                        )
                        .addAllByzantineValidators(emptyList())
                        .setHash(ByteString.copyFrom(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)))
                        .setLastCommitInfo(
                            Types.LastCommitInfo.newBuilder()
                                .setRound(1)
                                .addAllVotes(emptyList())
                                .build()
                        )
                        .build()
                )
                .setRes(
                    ResponseBeginBlock.newBuilder()
                        .addAllEvents(
                            listOf(
                                Types.Event.newBuilder()
                                    .setType("testEventType1")
                                    .build()
                            )
                        )
                        .build()
                )
                .build()
        )
        assertThat(reply.javaClass).isEqualTo(ListenBeginBlockResponse::class.java)

        val consumer = TestProtoConsumer<String, ListenBeginBlockRequest>(
            config = config,
            bootstrapServers = kafka.bootstrapServers,
            schemaRegistryUrl = schemaRegistry.baseUrl,
            topic = topicConfig.getString(ListenTopic.BEGIN_BLOCK.topic),
            valueClass = ListenBeginBlockRequest::class.java
        )
        consumer.consumeAndClose()
        assertThat(consumer.messages.size).isEqualTo(1)
        assertThat(consumer.messages[0].value().req.header.height).isEqualTo(1)
        assertThat(consumer.messages[0].value().res.eventsList[0].type).isEqualTo("testEventType1")
    }

    @Test
    fun listenEndBlock(): Unit = runBlocking {
        grpcCleanupRule.register(
            InProcessServerBuilder.forName("listenEndBlock").directExecutor()
                .addService(AbciListenerService(topicConfig, producer))
                .build()
                .start()
        )

        val reply: ListenEndBlockResponse = listenEndBlockStub.listenEndBlock(
            ListenEndBlockRequest.newBuilder()
                .setReq(RequestEndBlock.newBuilder().setHeight(1).build())
                .setRes(ResponseEndBlock.newBuilder().build())
                .build()
        )
        assertThat(reply.javaClass).isEqualTo(ListenEndBlockResponse::class.java)

        val consumer = TestProtoConsumer<String, ListenEndBlockRequest>(
            config = config,
            bootstrapServers = kafka.bootstrapServers,
            schemaRegistryUrl = schemaRegistry.baseUrl,
            topic = topicConfig.getString(ListenTopic.END_BLOCK.topic),
            valueClass = ListenEndBlockRequest::class.java
        )
        consumer.consumeAndClose()
        assertThat(consumer.messages.size).isEqualTo(1)
        assertThat(consumer.messages[0].value().req.height).isEqualTo(1)
        assertThat(consumer.messages[0].value().res.eventsList.size).isEqualTo(0)
    }

    @Test
    fun listenDeliverTx(): Unit = runBlocking {
        grpcCleanupRule.register(
            InProcessServerBuilder.forName("listenDeliverTx").directExecutor()
                .addService(AbciListenerService(topicConfig, producer))
                .build()
                .start()
        )

        val reply: ListenDeliverTxResponse = listenDeliverTxStub.listenDeliverTx(
            ListenDeliverTxRequest.newBuilder()
                .setBlockHeight(1)
                .setReq(RequestDeliverTx.newBuilder().setTx(ByteString.copyFrom("testTx1".toByteArray())).build())
                .setRes(ResponseDeliverTx.newBuilder().setCode(1).build())
                .build()
        )
        assertThat(reply.javaClass).isEqualTo(ListenDeliverTxResponse::class.java)

        val consumer = TestProtoConsumer<String, ListenDeliverTxRequest>(
            config = config,
            bootstrapServers = kafka.bootstrapServers,
            schemaRegistryUrl = schemaRegistry.baseUrl,
            topic = topicConfig.getString(ListenTopic.DELIVER_TX.topic),
            valueClass = ListenDeliverTxRequest::class.java
        )
        consumer.consumeAndClose()
        assertThat(consumer.messages.size).isEqualTo(1)
        assertThat(consumer.messages[0].value().blockHeight).isEqualTo(1)
        assertThat(consumer.messages[0].value().req.tx.toStringUtf8()).isEqualTo("testTx1")
        assertThat(consumer.messages[0].value().res.code).isEqualTo(1)
    }

    @Test
    fun listenCommit(): Unit = runBlocking {
        grpcCleanupRule.register(
            InProcessServerBuilder.forName("listenCommit").directExecutor()
                .addService(AbciListenerService(topicConfig, producer))
                .build()
                .start()
        )

        val changeSet = mutableListOf<StoreKVPair>()
        for (i in 1..2000) {
            changeSet.add(
                StoreKVPair.newBuilder()
                    .setStoreKey("mockStore1")
                    .setKey(ByteString.copyFrom("key$i".toByteArray()))
                    .setValue(ByteString.copyFrom("val$i".toByteArray()))
                    .setDelete(false)
                    .build()
            )
        }

        val reply: ListenCommitResponse = listenCommitStub.listenCommit(
            ListenCommitRequest.newBuilder()
                .setBlockHeight(1)
                .setRes(
                    ResponseCommit.newBuilder()
                        .setData(ByteString.copyFrom("data123".toByteArray()))
                        .build()
                )
                .addAllChangeSet(changeSet)
                .build()
        )
        assertThat(reply.javaClass).isEqualTo(ListenCommitResponse::class.java)

        val consumer = TestProtoConsumer<String, ListenCommitRequest>(
            config = config,
            bootstrapServers = kafka.bootstrapServers,
            schemaRegistryUrl = schemaRegistry.baseUrl,
            topic = topicConfig.getString(ListenTopic.COMMIT.topic),
            valueClass = ListenCommitRequest::class.java
        )
        consumer.consumeAndClose()
        assertThat(consumer.messages.size).isEqualTo(1)
        assertThat(consumer.messages[0].value().blockHeight).isEqualTo(1)
        assertThat(consumer.messages[0].value().res.data.toStringUtf8()).isEqualTo("data123")
        assertThat(consumer.messages[0].value().changeSetList[0].storeKey).isEqualTo("mockStore1")
    }
}
