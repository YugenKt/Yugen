package yugen.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused") // without it doesn't have enough to infer
internal inline fun <reified T> T.getLogger(): Logger {
    if (T::class.isCompanion) {
        return LoggerFactory.getLogger(T::class.java.enclosingClass)
    }
    return LoggerFactory.getLogger(T::class.java)
}