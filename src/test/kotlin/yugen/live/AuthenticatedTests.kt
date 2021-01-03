package yugen.live

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import yugen.Yugen
import yugen.events.DispatchEvent
import yugen.events.EventHandler

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticatedTests {
    private lateinit var client: Yugen

    @BeforeAll
    fun setup() {
        val token = javaClass.getResource("/token.txt").readText()
        client = Yugen(token)
    }

    @Test
    fun `client should fail to connect`() {
        assertThrows<IllegalArgumentException>("Invalid token provided") {
            runBlocking {
                Yugen("").connect()
            }
        }
    }

    @Test
    fun `client should connect`() {
        assertDoesNotThrow {
            runBlocking {
                client.eventBus.registerEvents(object {
                    @EventHandler fun test(e: DispatchEvent) {
                        println(e)
                    }
                })
                client.connect()
            }
        }
    }

    @Test
    fun pause() {
        Thread.sleep(10000000)
    }
}