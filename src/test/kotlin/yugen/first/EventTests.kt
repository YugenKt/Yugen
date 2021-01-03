package yugen.first

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import yugen.events.Event
import yugen.events.EventBus
import yugen.events.EventHandler
import kotlin.test.assertEquals

class EventTests {
    @Test
    fun `should do nothing when the event has no handlers`() {
        runBlocking {
            val eventBus = EventBus()
            var fired = false

            eventBus.registerEvents(object {
                @EventHandler fun shouldntFire(e: SomeOtherEvent) {
                    fired = true
                }
            })

            eventBus.fireEvent(TestEvent())
            assertEquals(false, fired)
        }
    }

    @Test
    fun `throws an exception when trying to register an event handler without parameters`() {
        assertThrows<UnsupportedOperationException> {
            val eventBus = EventBus()
            eventBus.registerEvents(object {
                @EventHandler fun test() {}
            })
        }
    }

    @Test
    fun `throws an exception when trying to register an event handler with too many parameters`() {
        assertThrows<IllegalArgumentException> {
            val eventBus = EventBus()
            eventBus.registerEvents(object {
                @EventHandler fun test(e: TestEvent, e2: TestEvent) {}
            })
        }
    }

    @Test
    fun `throws an exception when trying to register an event handler with a non-event parameter`() {
        assertThrows<IllegalArgumentException> {
            val eventBus = EventBus()
            eventBus.registerEvents(object {
                @EventHandler fun test(e: Any) {}
            })
        }
    }

    @Test
    fun `fires an event handler when the event is registered`() {
        runBlocking {
            val eventBus = EventBus()
            var fired = false

            eventBus.registerEvents(object {
                @EventHandler
                fun test(e: TestEvent) {
                    fired = true
                }
            })
            eventBus.fireEvent(TestEvent())

            assertEquals(true, fired)
        }
    }

    class TestEvent : Event
    class SomeOtherEvent : Event
}