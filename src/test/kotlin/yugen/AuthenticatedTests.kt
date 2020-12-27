package yugen

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*

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
                client.connect()
            }
        }
    }
}