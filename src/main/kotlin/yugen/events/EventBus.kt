package yugen.events

import yugen.util.getLogger
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.hasAnnotation

internal class EventBus {
    companion object {
        private val logger = getLogger()
    }

    private val eventHandlers: MutableMap<KClass<out Event>, MutableMap<Any, MutableList<KCallable<*>>>> = mutableMapOf()

    /**
     * Registers a [class]'s event handler methods. To mark a method as an event handler, annotate them with
     * [EventHandler].
     */
    fun registerEvents(`class`: Any) {
        `class`::class.members.filter { it.hasAnnotation<EventHandler>() }
            .forEach {
                // the first parameter of a method is an instance parameter
                if (it.parameters.count() == 1)
                    throw UnsupportedOperationException(
                        "Unable to infer event type as there are no parameters on the handler method $it")

                if (it.parameters.count() > 2)
                    throw IllegalArgumentException(
                        "Too many parameters on handler method $it (${it.parameters.count()} > 1)")

                val firstParam = it.parameters[1].type.classifier as KClass<*>
                if (firstParam.java.isInstance(Event::class.java))
                    throw IllegalArgumentException("First argument on $it is not an Event type")

                @Suppress("UNCHECKED_CAST") // this is a safe cast due to the sanity check above
                val event = firstParam as KClass<out Event>

                if (!eventHandlers.containsKey(event))
                    eventHandlers[event] = mutableMapOf()

                if (!eventHandlers[event]!!.containsKey(`class`))
                    eventHandlers[event]!![`class`] = mutableListOf()

                eventHandlers[event]!![`class`]!!.add(it)
                logger.trace("event handler for $event registered at $it")
            }
    }

    internal suspend fun fireEvent(event: Event) {
        eventHandlers[event::class]?.forEach { `class` ->
            `class`.value.forEach {
                try {
                    logger.trace("dispatching event ${event::class} to $it")
                    it.callSuspend(`class`.key, event)
                } catch (e: IllegalArgumentException) {
                    // shouldn't be able to get here?
                    throw IllegalStateException("$it has an invalid argument for the event its registered for?", e)
                }
            }
        }
    }
}