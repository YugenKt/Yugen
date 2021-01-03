package yugen

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.*
import yugen.events.DispatchEvent
import yugen.events.EventBus
import yugen.rest.Routes
import yugen.rest.UserAgentInterceptor
import yugen.util.gateway.CloseCode
import yugen.util.gateway.Opcode
import yugen.util.gateway.intentMap
import yugen.util.getLogger
import yugen.util.json
import yugen.util.send
import kotlin.concurrent.thread

/**
 * A Yugen client. The provided [token] will be used to authenticate to Discord.
 */
class Yugen(private val token: String) {
    companion object {
        private val logger = getLogger()
    }
    private val okHttp = OkHttpClient.Builder().addNetworkInterceptor(UserAgentInterceptor()).build()
    val eventBus = EventBus()

    private lateinit var gatewayWsUrl: String
    private lateinit var wsClient: WebSocket

    /**
     * Connects the client to Discord.
     */
    suspend fun connect() {
        logger.trace("starting connection")

        val gatewayResponse = Routes.GetGateway.execute(token = token)
        if (gatewayResponse.code == 401) throw IllegalArgumentException("Invalid token provided.")

        @Suppress("BlockingMethodInNonBlockingContext")
        val gatewayJson = gatewayResponse.body!!.json()

        gatewayWsUrl = gatewayJson["url"].asString + "?v=${YugenOptions.gatewayVersion}&encoding=json"
        logger.trace("using $gatewayWsUrl")

        var requiredIntents = 0
        eventBus.eventHandlers.keys
            .map { intentMap[it] }
            .forEach { logger.trace(it.toString()); it?.forEach { int -> requiredIntents += int.id } }

        logger.trace("required intents calculated as $requiredIntents")

        wsClient = okHttp.newWebSocket(
            Request.Builder().url(gatewayWsUrl).build(), YugenWebsocketListener(token, eventBus, requiredIntents))
    }

    private class YugenWebsocketListener(
        private val token: String,
        private val eventBus: EventBus,
        private val requiredIntents: Int
    ): WebSocketListener() {
        companion object {
            private val logger = getLogger()
        }

        private var heartbeatAcked = true
        private var sinceLastAck = 0L
        private var seq: Int? = null

        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.debug("Connected to gateway")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runBlocking {
                onMessageSuspend(webSocket, text)
            }
        }

        suspend fun onMessageSuspend(webSocket: WebSocket, text: String) {
            logger.trace("<- $text")
            val parsedData = JsonParser.parseString(text).asJsonObject

            if (parsedData.has("s") && !parsedData["s"].isJsonNull) {
                seq = parsedData["s"].asInt
                logger.trace("seq updated to $seq")
            }

            when (val op = parsedData["op"].asInt) {
                Opcode.HELLO -> {
                    // deal with HELLO here instead of sending them off
                    val helloData = parsedData["d"].asJsonObject
                    val heartbeatInterval = helloData["heartbeat_interval"].asInt

                    thread(name = "Heartbeat") {
                        while (true) {
                            Thread.sleep(heartbeatInterval.toLong())
                            if (!heartbeatAcked) {
                                logger.warn("Gateway didn't respond to heartbeat")
                                webSocket.close(1008, "heartbeat not acked within heartbeat interval")
                                return@thread
                            }
                            logger.trace("heartbeat")
                            if (!webSocket.send(mapOf("op" to Opcode.HEARTBEAT, "d" to seq))) {
                                logger.error("Failed to send heartbeat")
                            }
                            heartbeatAcked = false
                            sinceLastAck = System.currentTimeMillis()
                        }
                    }

                    // send off identify payload
                    val identify = mapOf(
                        "op" to 2,
                        "d" to mapOf(
                            "token" to token,
                            "intents" to requiredIntents,
                            "properties" to mapOf(
                                "\$os" to System.getProperty("os.name"),
                                "\$browser" to "Yugen",
                                "\$device" to "Yugen"
                            )
                        )
                    )

                    webSocket.send(identify)
                }

                Opcode.HEARTBEAT -> {
                    logger.trace("sent heartbeat back to discord")
                    webSocket.send(mapOf("op" to Opcode.HEARTBEAT_ACK))
                }

                Opcode.HEARTBEAT_ACK -> {
                    logger.trace("heartbeat ack took ${System.currentTimeMillis() - sinceLastAck}ms")
                    sinceLastAck = 0
                    heartbeatAcked = true
                }

                Opcode.DISPATCH -> {
                    eventBus.fireEvent(DispatchEvent(parsedData["d"].asJsonObject))
                }

                else -> throw NotImplementedError("opcode $op")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            logger.trace("websocket closing: $code -> $reason")
            logger.debug("Disconnected from gateway")
            if (code == CloseCode.DISALLOWED_INTENTS) {
                logger.error("You have event handlers for events that required privileged intents, but the " +
                        "privileged intents are not enabled. Will not reconnect to the gateway.")
                return
           }
        }
    }
}