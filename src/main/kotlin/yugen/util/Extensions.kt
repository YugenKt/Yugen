package yugen.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.coroutines.resumeWithException

private val gson = Gson()

@Suppress("EXPERIMENTAL_API_USAGE")
internal suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response, null)
            }

            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancelled
            }
        }
    }
}

@Suppress("unused") // without it doesn't have enough to infer
internal inline fun <reified T> T.getLogger(): Logger =
    if (T::class.isCompanion) {
        LoggerFactory.getLogger(T::class.java.enclosingClass)
    } else {
        LoggerFactory.getLogger(T::class.java)
    }

internal fun WebSocket.send(data: Map<String, Any>) = this.send(gson.toJson(data, Map::class.java))

internal fun ResponseBody.json() = JsonParser.parseString(this.string()).asJsonObject