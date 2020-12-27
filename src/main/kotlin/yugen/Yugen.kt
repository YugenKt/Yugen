package yugen

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import okhttp3.OkHttpClient
import yugen.rest.Routes
import yugen.rest.UserAgentInterceptor
import yugen.rest.await
import yugen.util.getLogger

/**
 * A Yugen client. The provided [token] will be used to authenticate to Discord.
 */
class Yugen(private val token: String) {
    private val okHttp = OkHttpClient.Builder().addNetworkInterceptor(UserAgentInterceptor()).build()
    private val parser: Parser = Parser.default()
    private val logger = getLogger()
    private lateinit var gatewayWsUrl: String

    /**
     * Connects the client to Discord.
     */
    suspend fun connect() {
        logger.trace("starting connection")

        val gatewayResponse = okHttp.newCall(Routes.GetGateway.toRequest(token = token)).await()
        if (gatewayResponse.code == 401) throw IllegalArgumentException("Invalid token provided.")

        val gatewayJson = parser.parse(gatewayResponse.body!!.byteStream()) as JsonObject

        gatewayWsUrl = gatewayJson.string("url")!!
    }
}