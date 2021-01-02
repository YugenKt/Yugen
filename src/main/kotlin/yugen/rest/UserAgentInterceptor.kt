package yugen.rest

import okhttp3.Interceptor
import okhttp3.Response
import yugen.YugenOptions

internal class UserAgentInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder().header("User-Agent", YugenOptions.userAgent).build()
        )
    }
}