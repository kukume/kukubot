package me.kuku.mirai.config

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import net.mamoe.mirai.internal.spi.*
import net.mamoe.mirai.utils.*
import org.asynchttpclient.*
import kotlin.coroutines.*

public class UnidbgFetchQsign(private val server: String, private val key: String, coroutineContext: CoroutineContext) :
    EncryptService, CoroutineScope {

    override val coroutineContext: CoroutineContext =
        coroutineContext + SupervisorJob(coroutineContext[Job]) + CoroutineExceptionHandler { context, exception ->
            when (exception) {
                is CancellationException -> {
                    // ...
                }
                else -> {
                    logger.warning({ "with ${context[CoroutineName]}" }, exception)
                }
            }
        }

    private val client = Dsl.asyncHttpClient(
        DefaultAsyncHttpClientConfig.Builder()
            .setKeepAlive(true)
            .setUserAgent("curl/7.61.0")
            .setRequestTimeout(90_000)
            .setConnectTimeout(30_000)
            .setReadTimeout(180_000)
    )

    private var channel0: EncryptService.ChannelProxy? = null

    private val channel: EncryptService.ChannelProxy get() = channel0 ?: throw IllegalStateException("need initialize")

    private val token = java.util.concurrent.atomic.AtomicLong(0)

    override fun initialize(context: EncryptServiceContext) {
        val device = context.extraArgs[EncryptServiceContext.KEY_DEVICE_INFO]
        val qimei36 = context.extraArgs[EncryptServiceContext.KEY_QIMEI36]
        val channel = context.extraArgs[EncryptServiceContext.KEY_CHANNEL_PROXY]

        logger.info("Bot(${context.id}) initialize by $server")

        channel0 = channel

        if (token.get() == 0L) {
            val uin = context.id
            @OptIn(MiraiInternalApi::class)
            register(
                uin = uin,
                androidId = device.androidId.decodeToString(),
                guid = device.guid.toUHexString(),
                qimei36 = qimei36
            )
            coroutineContext.job.invokeOnCompletion {
                try {
                    destroy(uin = uin)
                } catch (cause : Throwable) {
                    logger.warning("Bot(${uin}) destroy", cause)
                } finally {
                    token.compareAndSet(uin, 0)
                }
            }
        }

        logger.info("Bot(${context.id}) initialize complete")
    }

    private fun register(uin: Long, androidId: String, guid: String, qimei36: String) {
        val response = client.prepareGet("${server}/register")
            .addQueryParam("uin", uin.toString())
            .addQueryParam("android_id", androidId)
            .addQueryParam("guid", guid)
            .addQueryParam("qimei36", qimei36)
            .addQueryParam("key", key)
            .execute().get()
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)
        check(body.code == 0) { body.message }

        logger.info("Bot(${uin}) register, ${body.message}")
    }

    private fun destroy(uin: Long) {
        val response = client.prepareGet("${server}/destroy")
            .addQueryParam("uin", uin.toString())
            .addQueryParam("key", key)
            .execute().get()
        if (response.statusCode == 404) return
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)

        logger.info("Bot(${uin}) destroy, ${body.message}")
    }

    override fun encryptTlv(context: EncryptServiceContext, tlvType: Int, payload: ByteArray): ByteArray? {
        if (tlvType != 0x544) return null
        val command = context.extraArgs[EncryptServiceContext.KEY_COMMAND_STR]

        val data = customEnergy(uin = context.id, salt = payload, data = command)

        return data.hexToBytes()
    }

    private fun customEnergy(uin: Long, salt: ByteArray, data: String): String {
        val response = client.prepareGet("${server}/custom_energy")
            .addQueryParam("uin", uin.toString())
            .addQueryParam("salt", salt.toUHexString(""))
            .addQueryParam("data", data)
            .execute().get()
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)
        check(body.code == 0) { body.message }

        logger.debug("Bot(${uin}) custom_energy ${data}, ${body.message}")

        return Json.decodeFromJsonElement(String.serializer(), body.data)
    }

    override fun qSecurityGetSign(
        context: EncryptServiceContext,
        sequenceId: Int,
        commandName: String,
        payload: ByteArray
    ): EncryptService.SignResult? {
        if (commandName == "StatSvc.register") {
            if (token.compareAndSet(0, context.id)) {
                launch(CoroutineName("RequestToken")) {
                    val uin = token.get()
                    while (isActive) {
                        val interval = System.getProperty(REQUEST_TOKEN_INTERVAL, "2400000").toLong()
                        if (interval <= 0L) break
                        if (interval < 600_000) logger.warning("$REQUEST_TOKEN_INTERVAL=${interval} < 600_000 (ms)")
                        delay(interval)
                        val request = try {
                            requestToken(uin = uin)
                        } catch (cause: Throwable) {
                            logger.error(cause)
                            continue
                        }
                        callback(uin = uin, request = request)
                    }
                }
            }
        }

        if (commandName !in CMD_WHITE_LIST) return null

        val data = sign(uin = context.id, cmd = commandName, seq = sequenceId, buffer = payload)

        callback(uin = context.id, request = data.request)

        return EncryptService.SignResult(
            sign = data.sign.hexToBytes(),
            token = data.token.hexToBytes(),
            extra = data.extra.hexToBytes()
        )
    }

    private fun sign(uin: Long, cmd: String, seq: Int, buffer: ByteArray): SignResult {
        val response = client.preparePost("${server}/sign")
            .addFormParam("uin", uin.toString())
            .addFormParam("cmd", cmd)
            .addFormParam("seq", seq.toString())
            .addFormParam("buffer", buffer.toUHexString(""))
            .execute().get()
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)
        check(body.code == 0) { body.message }

        logger.debug("Bot(${uin}) sign ${cmd}, ${body.message}")

        return Json.decodeFromJsonElement(SignResult.serializer(), body.data)
    }

    private fun requestToken(uin: Long): List<RequestCallback> {
        val response = client.prepareGet("${server}/request_token")
            .addQueryParam("uin", uin.toString())
            .execute().get()
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)
        check(body.code == 0) { body.message }

        logger.info("Bot(${uin}) request_token, ${body.message}")

        return Json.decodeFromJsonElement(ListSerializer(RequestCallback.serializer()), body.data)
    }

    private fun submit(uin: Long, cmd: String, callbackId: Int, buffer: ByteArray) {
        val response = client.prepareGet("${server}/submit")
            .addQueryParam("uin", uin.toString())
            .addQueryParam("cmd", cmd)
            .addQueryParam("callback_id", callbackId.toString())
            .addQueryParam("buffer", buffer.toUHexString(""))
            .execute().get()
        val body = Json.decodeFromString(DataWrapper.serializer(), response.responseBody)
        check(body.code == 0) { body.message }

        logger.debug("Bot(${uin}) submit ${cmd}, ${body.message}")
    }

    private fun callback(uin: Long, request: List<RequestCallback>) {
        launch(CoroutineName("SendMessage")) {
            for (callback in request) {
                logger.verbose("Bot(${uin}) sendMessage ${callback.cmd} ")
                val result = channel.sendMessage(
                    remark = "mobileqq.msf.security",
                    commandName = callback.cmd,
                    uin = 0,
                    data = callback.body.hexToBytes()
                )
                if (result == null) {
                    logger.debug("${callback.cmd} ChannelResult is null")
                    continue
                }

                submit(uin = uin, cmd = result.cmd, callbackId = callback.id, buffer = result.data)
            }
        }
    }

    override fun toString(): String {
        return "UnidbgFetchQsign(server=${server}, uin=${token})"
    }

    public companion object {
        @JvmStatic
        internal val CMD_WHITE_LIST = UnidbgFetchQsign::class.java.getResource("cmd.txt")!!.readText().lines()

        @JvmStatic
        internal val logger: MiraiLogger = MiraiLogger.Factory.create(UnidbgFetchQsign::class)

        @JvmStatic
        public val REQUEST_TOKEN_INTERVAL: String = "xyz.cssxsh.mirai.tool.UnidbgFetchQsign.token.interval"
    }
}

@Serializable
private data class DataWrapper(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val message: String = "",
    @SerialName("data")
    val data: JsonElement
)

@Serializable
private data class SignResult(
    @SerialName("token")
    val token: String = "",
    @SerialName("extra")
    val extra: String = "",
    @SerialName("sign")
    val sign: String = "",
    @SerialName("o3did")
    val o3did: String = "",
    @SerialName("requestCallback")
    val request: List<RequestCallback> = emptyList()
)

@Serializable
private data class RequestCallback(
    @SerialName("body")
    val body: String,
    @SerialName("callback_id")
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("callbackId", "callback_id")
    val id: Int,
    @SerialName("cmd")
    val cmd: String
)
