package yugen.events

import yugen.util.gateway.Intent

/**
 * An event with a list of required [intents], if any.
 */
open class Event(val intents: List<Intent>? = null)