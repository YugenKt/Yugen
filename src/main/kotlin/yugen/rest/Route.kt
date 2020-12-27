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
     * A route pointing to a [url] using the given [method] for the request.
     */
    internal class Route(private val method: String, private val url: String) {
        /**
         * Turns the route into a request with the given [body] and [params]. Optionally, a [token] can be provided
         * for authenticated requests.
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