package yugen.util.gateway

/**
 * Gateway intents.
 */
enum class Intent(private val id: Int) {
    GUILDS(1 shl 0),
    GUILD_MEMBERS(1 shl 1),
    GUILD_BANS(1 shl 2),
    GUILD_EMOJIS(1 shl 3),
    GUILD_INTEGRATIONS(1 shl 4),
    GUILD_WEBHOOKS(1 shl 5),
    GUILD_INVITES(1 shl 6),
    GUILD_VOICE_STATES(1 shl 7),
    GUILD_PRESENCES(1 shl 8),
    GUILD_MESSAGES(1 shl 9),
    GUILD_MESSAGE_REACTIONS(1 shl 10),
    GUILD_MESSAGE_TYPING(1 shl 11),

    DIRECT_MESSAGES(1 shl 12),
    DIRECT_MESSAGE_REACTIONS(1 shl 13),
    DIRECT_MESSAGE_TYPING(1 shl 14);

    operator fun plus(other: Intent): Int {
        return this.id + other.id
    }
}