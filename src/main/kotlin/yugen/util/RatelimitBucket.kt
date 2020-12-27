package yugen.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration

/**
 * An automatically refilling ratelimit bucket which refills to the given amount of [tokens].
 *
 * The bucket automatically starts the refill cycle when a token is *taken*, not on a constant loop.
 *
 * @param refillEvery The amount of time between each refill.
 */
internal class RatelimitBucket(
    private val tokens: Int,
    refillEvery: Duration
) {
    private val tokenRefillLock = Mutex(true)
    private val tokensAvailable = Mutex(false)

    // lock owners
    private val refillingLock = Object() // whilst the bucket is being refilled
    private val consumptionLock = Object() // when the bucket is refilled and is waiting for tokens to be removed

    private var tokensLeft = tokens

    init {
        GlobalScope.launch {
            while (true) {
                tokenRefillLock.withLock(refillingLock) {
                    delay(refillEvery.toMillis())
                    tokensLeft = tokens
                }
                tokenRefillLock.lock(consumptionLock)
                tokensAvailable.unlock()
            }
        }
    }

    /**
     * Attempts to consume an [amount] of tokens from the bucket.
     */
    fun tryConsume(amount: Int = 1): Boolean {
        if (amount > tokens) {
            throw IllegalArgumentException(
                "Attempting to consume more tokens than this bucket is capable of holding ($amount > $tokens)"
            )
        }

        return if (tokensLeft >= amount) {
            try {
                tokenRefillLock.unlock(consumptionLock)
            } catch (e: IllegalStateException) {
                // ignored, means it's being refilled
            }

            tokensLeft -= amount

            true
        } else {
            false
        }
    }

    /**
     * Consumes an [amount] of tokens from the bucket. This method will suspend until the tokens are available.
     */
    suspend fun consume(amount: Int = 1) {
        if (tryConsume(amount)) return

        tokensAvailable.lock() // wait until the refill unlocks this

        tokensAvailable.withLock {
            tryConsume(amount) // assume that call will always succeed, the first tryConsume call at the start of the
                               // fun throws an exception if it leads to an impossible amount of tokens being claimed.
        }
    }
}