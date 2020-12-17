package yugen.rest

import okhttp3.Request
import okhttp3.RequestBody
import yugen.YugenOptions

/**
 * Discord API routes.
 */
internal object Routes {
    val GetGateway = Route("GET", "/gateway/bot")

    /**
     * A route.
     *
     * @param method The HTTP method for this route.
     * @param url The API URL to this route.
     */
    internal class Route(private val method: String, private val url: String) {
        /**
         * Turns the route into a request.
         *
         * @param params The parameters to fill in the URL.
         * @param body A request body.
         * @param token The bot token.
         */
        fun toRequest(params: Map<String, String>? = null, body: RequestBody? = null, token: String? = null): Request {
            val request = Request.Builder().url(
                YugenOptions.apiBase + run {
                    var formattedUrl = url
                    params?.forEach {
                        formattedUrl = formattedUrl.replace(":" + it.key, it.value)
                    }
                    formattedUrl
                }
            ).method(method, body)

            if (token != null)
                request.addHeader("Authorization", "Bot $token")

            return request.build()
        }
    }
}