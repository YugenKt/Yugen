package yugen.util

import yugen.YugenOptions
import java.time.Instant

/**
 * A data class that wraps a [snowflake] and can get extra information from it.
 */
data class Snowflake(private var snowflake: Long) {
    /**
     * The same as the default constructor, except [snowflake] is a String.
     */
    constructor(snowflake: String) : this(snowflake.toLong())

    /**
     * The ID of the worker that generated this snowflake.
     */
    val workerId = (snowflake and 0x3E0000) shr 17

    /**
     * The ID of the process that generated this snowflake.
     */
    val processId = (snowflake and 0x1F000) shr 12

    /**
     * The increment on this ID. Incremented for every ID generated on the process identified by [processId].
     */
    val increment = snowflake and 0xFFF

    /**
     * The snowflake's timestamp as an Instant.
     */
    val instant = Instant.ofEpochMilli((snowflake shr 22) + YugenOptions.discordEpoch)

    /**
     * The snowflake itself as a long.
     */
    val long = snowflake

    /**
     * Compare two snowflake instances by their Long values.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Snowflake) return false
        if (snowflake != other.snowflake) return false

        return true
    }

    /**
     * Returns a string representation of the Snowflake.
     */
    override fun toString() =
        "Snowflake($snowflake, workerId=$workerId, processId=$processId, increment=$increment, instant=$instant)"

    /**
     * Generate a hashcode based on the snowflake's Long value.
     */
    override fun hashCode() = snowflake.hashCode()
}