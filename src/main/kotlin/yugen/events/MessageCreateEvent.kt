package yugen.events

import yugen.structures.Message
import yugen.util.gateway.Intent

/**
 * Fired when a message is created.
 */
data class MessageCreateEvent(val message: Message): Event
