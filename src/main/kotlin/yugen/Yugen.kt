package yugen

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import okhttp3.OkHttpClient
import yugen.rest.Routes
import yugen.rest.UserAgentInterceptor
import yugen.rest.await

/**
 * A Yugen client.
 *
 * @property token The bot token that will be used for this client.
 */
class Yugen(private val token: String) {
    private val okHttp = OkHttpClient.Builder().addNetworkInterceptor(UserAgentInterceptor()).build()
    private val parser: Parser = Parser.default()
    private lateinit var gatewayWsUrl: String

    /**
     * Connects the client to Discord.
     */
    suspend fun connect() {
        val gatewayResponse = okHttp.newCall(Routes.GetGateway.toRequest(token = token)).await()
        if (gatewayResponse.code == 401) throw IllegalArgumentException("Invalid token provided.")

        @Suppress("BlockingMethodInNonBlockingContext")
        val gatewayJson = parser.parse(gatewayResponse.body!!.string()) as JsonObject

        gatewayWsUrl = gatewayJson.string("url")!!
    }
}