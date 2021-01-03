package yugen.events

import yugen.structures.Message

/**
 * Fired when a message is created.
 */
data class MessageCreateEvent(val message: Message): Event
