package com.example.android.architecture.blueprints.beetv.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitBuilder {

    const val REST_DOMAIN_URL = "https://beetv.hitek.com.vn:8889"
    const val RECORDER_SERVER_IP = "http://23.237.182.178:8888"
//    const val BASE_URL = "https://server.hitek.com.vn:8889"
    const val BASE_PATH = "/api/v1/"
//    private const val DEFAULT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjp7ImRldmljZV9pZCI6ImQ1ZmNlNGYwLWY5YmMtMTFlYS1hNjBjLTMzMTM2NTI3ZDAyNSIsInJvbGUiOiJVU0VSIn0sInJvbGUiOiJVU0VSIiwiZXhwIjoiMjAyMC0xMS0xOVQxNjoyNzoxMy43MDhaIn0.AYqdnCVbKMhmSD8BL3ec3BkC8_n82NoWu1ssdNSKE-8"
//    private const val BASE_URL = "http://milo.hitek.com.vn:8881/api/v1/"

    private fun getRetrofit(): Retrofit {


        val builder = OkHttpClient.Builder(
        )

//        builder.connectTimeout(1, TimeUnit.MILLISECONDS)
//        builder.readTimeout(1, TimeUnit.MILLISECONDS)
//        builder.writeTimeout(1, TimeUnit.MILLISECONDS)

        builder.connectTimeout(2, TimeUnit.MINUTES)
        builder.readTimeout(2, TimeUnit.MINUTES)
        builder.writeTimeout(2, TimeUnit.MINUTES)
//        val sharedPref = BeeTVApplication.context.getSharedPreferences(
//                "BeeTV", Context.MODE_PRIVATE) ?: null
//
//        val token = sharedPref?.getString("token", "")

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(interceptor)
        builder.addInterceptor(MyServiceInterceptor.getInstance())
        builder.addInterceptor(ChangeableBaseUrlInterceptor.getInstance())
//        if (!token.isNullOrEmpty())
//        builder.addInterceptor { chain ->
//            val request: Request = chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
//            chain.proceed(request)
//        }
        val client = builder.build(
        )
        return Retrofit.Builder()
                .baseUrl(REST_DOMAIN_URL + BASE_PATH)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}