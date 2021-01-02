package yugen.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import okhttp3.WebSocket
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val gson = Gson()

@Suppress("unused") // without it doesn't have enough to infer
internal inline fun <reified T> T.getLogger(): Logger =
    if (T::class.isCompanion) {
        LoggerFactory.getLogger(T::class.java.enclosingClass)
    } else {
        LoggerFactory.getLogger(T::class.java)
    }

internal fun WebSocket.send(data: Map<String, Any>) = this.send(gson.toJson(data, Map::class.java))

internal fun ResponseBody.json() = JsonParser.parseString(this.string()).asJsonObject