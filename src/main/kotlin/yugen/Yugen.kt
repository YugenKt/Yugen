package yugen

import com.google.gson.JsonParser
import okhttp3.*
import yugen.rest.Routes
import yugen.rest.UserAgentInterceptor
import yugen.util.gateway.Opcode
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

        wsClient = okHttp.newWebSocket(Request.Builder().url(gatewayWsUrl).build(), YugenWebsocketListener(token))
    }

    private class YugenWebsocketListener(private val token: String): WebSocketListener() {
        companion object {
            private val logger = getLogger()
        }

        private var heartbeatAcked = true
        private var sinceLastAck = 0L

        override fun onOpen(webSocket: WebSocket, response: Response) {
            val identify = mapOf(
                "op" to 2,
                "d" to mapOf(
                   "token" to token,
                   "intents" to 0,
                   "properties" to mapOf(
                       "\$os" to System.getProperty("os.name"),
                       "\$browser" to "Yugen",
                       "\$device" to "Yugen"
                   )
                )
            )

            webSocket.send(identify)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.trace("message: $text")
            val parsedData = JsonParser.parseString(text).asJsonObject

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
                            if (!webSocket.send(mapOf("op" to Opcode.HEARTBEAT))) {
                                logger.error("Failed to send heartbeat")
                            }
                            sinceLastAck = System.currentTimeMillis()
                        }
                    }
                }

                Opcode.HEARTBEAT -> {
                    webSocket.send(mapOf("op" to Opcode.HEARTBEAT_ACK))
                }

                Opcode.HEARTBEAT_ACK -> {
                    logger.trace("heartbeat ack took ${System.currentTimeMillis() - sinceLastAck}ms")
                    sinceLastAck = 0
                    heartbeatAcked = true
                }

                else -> throw NotImplementedError("opcode $op")
            }
        }
    }
}