package yugen.util.gateway

object CloseCode {
    const val UNKNOWN_ERROR = 4000
    const val UNKNOWN_OPCODE = 4001
    const val DECODE_ERROR = 4002
    const val NOT_AUTHENTICATED = 4003
    const val AUTHENTICATION_FAILED = 4004
    const val ALREADY_AUTHENTICATED = 4005
    const val INVALID_SEQ =  4007
    const val RATE_LIMITED = 4008
    const val SESSION_TIMED_OUT = 4009
    const val INVALID_SHARD = 4010
    const val SHARDING_REQUIRED = 4011
    const val INVALID_API_VERSION = 4012
    const val INVALID_INTENTS = 4013
    const val DISALLOWED_INTENTS = 4014
}