package com.example.android.architecture.blueprints.beetv.data.api

import android.util.Log
import com.example.android.architecture.blueprints.beetv.data.api.RetrofitBuilder.BASE_PATH
import com.example.android.architecture.blueprints.beetv.data.api.RetrofitBuilder.RECORDER_SERVER_IP
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class ChangeableBaseUrlInterceptor : Interceptor {

    companion object {
        private var instance: ChangeableBaseUrlInterceptor? = null
        fun getInstance(): ChangeableBaseUrlInterceptor {
            if (instance == null) instance = ChangeableBaseUrlInterceptor()
            return instance!!
        }
    }


    @Volatile
    private var host: HttpUrl? = null

    @Volatile
    private var headersToRedact = emptySet<String>()

    fun setApiDomain(apiDomain: String) {
        this.host = apiDomain.toHttpUrlOrNull()
    }

    fun clearApiDomain() {
        host = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()


        if (host == null) return chain.proceed(request)

        var tempHost: HttpUrl? = host
        if (request.tag(String::class.java) == "RECORDER") {
            tempHost = "$RECORDER_SERVER_IP$BASE_PATH".toHttpUrlOrNull()
        }

        tempHost?.let {
            val newHost = HttpUrl.Builder()
                    .scheme(it.scheme)
                    .host(it.toUrl().toURI().host)
                    .port(it.port)
            for (pathSegment in request.url.pathSegments) {
                if (pathSegment.isNotBlank()) newHost.addPathSegment(pathSegment)
            }
            for (q in request.url.queryParameterNames) {
                newHost.addQueryParameter(q, request.url.queryParameter(q))
            }
            val newUrl = newHost.build()
            val oldUrl = request.url
            request = request.newBuilder().url(newUrl).build()
            HttpLoggingInterceptor.Logger.DEFAULT.log("--> Change request from $oldUrl to $newUrl")
        }

        return chain.proceed(request)
    }
}