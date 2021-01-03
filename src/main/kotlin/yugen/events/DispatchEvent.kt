package yugen.events

import com.google.gson.JsonObject
import yugen.util.gateway.Intent

/**
 * Fired when an event is dispatched from the gateway. The [data] is provided as a [JsonObject].
 *
 * **WARNING:** Listening to this event will enable *every* intent! This may lead to an accidental denial of service on
 * a larger bot. Be careful!
 */
data class DispatchEvent(val data: JsonObject) : Event(Intent.values().asList())