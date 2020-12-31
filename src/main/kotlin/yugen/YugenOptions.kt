package yugen

/**
 * The library options for Yugen. These control various aspects of the library such as what endpoints to use.
 */
internal object YugenOptions {
    /**
     * The base URL for API routes.
     */
    const val apiBase = "https://discord.com/api/v8"

    /**
     * The gateway version to use when connecting.
     */
    const val gatewayVersion = 8

    /**
     * The user agent the library uses to make requests to Discord.
     */
    val userAgent = "Yugen/${this.javaClass.`package`.implementationVersion ?: "DEVELOPMENT"}"
}