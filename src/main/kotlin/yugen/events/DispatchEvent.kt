package yugen.events

import com.google.gson.JsonObject

/**
 * Fired when an event is dispatched from the gateway. The [data] is provided as a [JsonObject].
 */
data class DispatchEvent(val data: JsonObject) : Event