package yugen.rest

import io.github.bucket4j.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import yugen.YugenOptions
import yugen.util.getLogger
import java.time.Duration
import java.time.Instant

/**
 * Discord API routes.
 */
internal object Routes {
    private val okHttp = OkHttpClient.Builder().addNetworkInterceptor(UserAgentInterceptor()).build()
    private val ratelimitBuckets = mutableMapOf<String, BlockingBucket>()

    val GetGateway = Route("GET", "/gateway/bot")

    /**
     * A route pointing to a [url] using the given [method] for the request. The [majorParam] is for ratelimit bucket
     * identification and is not strictly necessary.
     */
    internal class Route(private val method: String, private val url: String, private val majorParam: String? = null) {
        companion object {
            private val logger = getLogger()
        }

        private var ratelimitBucketIdentifier: String? = null

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

        /**
         * Executes this route with a ratelimit. Passes [body], [params], and [token] into [toRequest]. If this
         * route does not yet have a ratelimit bucket, one will be created based on the ratelimit headers of the
         * response. This only happens once.
         */
        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun execute(
            params: Map<String, String>? = null,
            body: RequestBody? = null,
            token: String? = null
        ): Response {
            logger.trace("executing route $url")

            if (ratelimitBucketIdentifier != null)
                ratelimitBuckets[ratelimitBucketIdentifier]!!.consume(1)

            val response = okHttp.newCall(toRequest(params, body, token)).await()

            var bucketName = response.headers["x-ratelimit-bucket"]
            if (majorParam != null)
                bucketName = bucketName?.plus("-${params?.get("majorParam")!!}")

            if (
                (ratelimitBucketIdentifier == null || ratelimitBucketIdentifier != bucketName) &&
                response.headers["x-ratelimit-bucket"] != null
            ) {
                ratelimitBucketIdentifier = bucketName!!
                val limit = response.headers["x-ratelimit-limit"]!!.toLong()
                val remaining = response.headers["x-ratelimit-remaining"]!!.toLong()
                val resetsAt = Instant.ofEpochMilli(
                    (response.headers["x-ratelimit-reset"]!!.toFloat() * 1000).toLong()
                )
                val duration = Duration.ofMillis(
                    (response.headers["x-ratelimit-reset-after"]!!.toFloat() * 1000).toLong()
                )

                ratelimitBuckets[ratelimitBucketIdentifier!!] = Bucket4j.builder().addLimit(
                    Bandwidth.classic(
                        limit,
                        Refill.intervallyAligned(limit, duration, resetsAt, false)
                    ).withInitialTokens(remaining)
                ).build().asScheduler()
                logger.trace("learnt new bucket ($ratelimitBucketIdentifier) of $limit/${duration.toMillis()}ms")
            }

            return response
        }
    }
}