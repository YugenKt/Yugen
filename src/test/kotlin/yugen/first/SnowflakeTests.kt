package yugen.first

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import yugen.util.Snowflake
import java.time.Instant
import kotlin.test.assertEquals

class SnowflakeTests {
    @ParameterizedTest
    @CsvSource(
        "308994132968210433, 1493740342133",
        "96269247411400704, 1443022777179",
        "128316294742147072, 1450663388668",
        "110373943822540800, 1446385598856",
        "135412374344695808, 1452355225884",
        "301379068941828096, 1491924769388",
        "545581357812678656, 1550147132114",
        "195156669108322313, 1466599375751",
        "794214679249158174, 1609425935328",
        "523033276802400256, 1544771250678"
    )
    fun `snowflake should produce correct instant`(snowflakeInput: Long, expectedMillis: Long) {
        assertEquals(Instant.ofEpochMilli(expectedMillis), Snowflake(snowflakeInput).instant)
    }

    @ParameterizedTest
    @CsvSource(
        "308994132968210433, 0",
        "96269247411400704, 0",
        "128316294742147072, 0",
        "110373943822540800, 0",
        "135412374344695808, 1",
        "301379068941828096, 2",
        "545581357812678656, 0",
        "195156669108322313, 0",
        "794214679249158174, 6",
        "523033276802400256, 2"
    )
    fun `snowflake produces correct worker ID`(snowflakeInput: Long, expectedWorkerId: Long) {
        assertEquals(expectedWorkerId, Snowflake(snowflakeInput).workerId)
    }

    @ParameterizedTest
    @CsvSource(
        "308994132968210433, 0",
        "96269247411400704, 3",
        "128316294742147072, 0",
        "110373943822540800, 6",
        "135412374344695808, 0",
        "301379068941828096, 0",
        "545581357812678656, 0",
        "195156669108322313, 0",
        "794214679249158174, 0",
        "523033276802400256, 0"
    )
    fun `snowflake produces correct process ID`(snowflakeInput: Long, expectedProcessId: Long) {
        assertEquals(expectedProcessId, Snowflake(snowflakeInput).processId)
    }

    @ParameterizedTest
    @CsvSource(
        "308994132968210433, 1",
        "96269247411400704, 0",
        "128316294742147072, 0",
        "110373943822540800, 0",
        "135412374344695808, 0",
        "301379068941828096, 0",
        "545581357812678656, 0",
        "195156669108322313, 9",
        "794214679249158174, 30",
        "523033276802400256, 0"
    )
    fun `snowflake produces correct increment`(snowflakeInput: Long, expectedIncrement: Long) {
        assertEquals(expectedIncrement, Snowflake(snowflakeInput).increment)
    }
}